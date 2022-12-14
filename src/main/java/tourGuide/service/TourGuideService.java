package tourGuide.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import tourGuide.helper.InternalTestHelper;
import tourGuide.tracker.Tracker;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;
import tourGuide.user.UserReward;
import tripPricer.Provider;
import tripPricer.TripPricer;


@Service
public class TourGuideService
{

    //=========================
    //=      Attributes       =
    //=========================
    private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
    private final GpsUtil gpsUtil;
    private final RewardsService rewardsService;
    private final TripPricer tripPricer = new TripPricer();
    public final Tracker tracker;
    boolean testMode = true;


    //=========================
    //=     Constructors      =
    //=========================
    public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService)
    {
        this.gpsUtil = gpsUtil;
        this.rewardsService = rewardsService;

        if (testMode)
        {
            logger.info("TestMode enabled");
            logger.debug("Initializing users");
            initializeInternalUsers();
            logger.debug("Finished initializing users");
        }

        tracker = new Tracker(this);
        addShutDownHook();
    }


    //=========================
    //=   Business Methods    =
    //=========================


    public Collection<UserReward> getUserRewards(User user)
    {
        return user.getUserRewards();
    }



    public VisitedLocation getUserLocation(User user)
    {
        return (user.getVisitedLocations().size() > 0) ? user.getLastVisitedLocation() : trackUserLocation(user);
    }



    public User getUser(String userName)
    {
        return internalUserMap.get(userName);
    }


    public List<User> getAllUsers()
    {
        return internalUserMap.values().stream().collect(Collectors.toList());
    }



    public void addUser(User user)
    {
        if (!internalUserMap.containsKey(user.getUserName()))
        {
            internalUserMap.put(user.getUserName(), user);
        }
    }



    public List<Provider> getTripDeals(User user)
    {
        int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();

        List<Provider> providers = tripPricer.getPrice(
                                                        tripPricerApiKey,
                                                        user.getUserId(),
                                                        user.getUserPreferences().getNumberOfAdults(),
                                                        user.getUserPreferences().getNumberOfChildren(),
                                                        user.getUserPreferences().getTripDuration(),
                                                        cumulatativeRewardPoints
                                                     );
        user.setTripDeals(providers);
        return providers;
    }





//    public VisitedLocation trackUserLocation(User user)
//    {
//        VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
//        user.addToVisitedLocations(visitedLocation);
//        rewardsService.calculateRewards(user);
//        return visitedLocation;
//    }



    public VisitedLocation trackUserLocation(User user)
    {
        return CompletableFuture.supplyAsync( () -> gpsUtil.getUserLocation(user.getUserId()) )
                                .thenApply  (
                                        visitedLocation -> {
                                            user.addToVisitedLocations(visitedLocation);
                                            rewardsService.calculateRewards(user);
                                            return visitedLocation;
                                        }
                                )
                                .join();
    }


    public void trackUsersLocation(Collection<User> users)
    {
        users.parallelStream().forEach(this::trackUserLocation);
    }


    public List<Attraction> getNearbyAttractions(VisitedLocation visitedLocation)
    {
        return Stream.concat(
                                gpsUtil.getAttractions()
                                        .stream()
                                        .filter( attraction -> rewardsService.isWithinAttractionProximity(attraction, visitedLocation.location)),

                                gpsUtil.getAttractions()
                                        .stream()
                                        .sorted(Comparator.comparing(attraction -> rewardsService.getDistance(visitedLocation.location, attraction)))
                                        .limit(5)
                            )
                    .distinct()
                    .collect(Collectors.toList());
    }



    public List<VisitedLocation> getAllVisitedLocationsOfAllUsers()
    {
        return getAllUsers().stream()
                .flatMap(user -> user.getVisitedLocations().stream())
                .collect(Collectors.toList());
    }


    private void addShutDownHook()
    {
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            public void run()
            {
                tracker.stopTracking();
            }
        });
    }

    /**********************************************************************************
     * Methods Below: For Internal Testing
     **********************************************************************************/
    private static final String tripPricerApiKey = "test-server-api-key";
    // Database connection will be used for external users, but for testing purposes internal users are provided and stored in memory
    private final Map<String, User> internalUserMap = new HashMap<>();

    private void initializeInternalUsers()
    {
        IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach( i -> {
                    String userName = "internalUser" + i;
                    String phone = "000";
                    String email = userName + "@tourGuide.com";
                    User user = new User(UUID.randomUUID(), userName, phone, email);
                    generateUserLocationHistory(user);

                    internalUserMap.put(userName, user);
                }
        );
        logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
    }

    private void generateUserLocationHistory(User user)
    {
        IntStream.range(0, 3).forEach(i -> {
                    user.addToVisitedLocations(
                            new VisitedLocation(
                                    user.getUserId(),
                                    new Location(generateRandomLatitude(),
                                            generateRandomLongitude()),
                                    getRandomTime()
                            )
                    );
                }
        );
    }

    private double generateRandomLongitude()
    {
        double leftLimit = -180;
        double rightLimit = 180;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    private double generateRandomLatitude()
    {
        double leftLimit = -85.05112878;
        double rightLimit = 85.05112878;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    private Date getRandomTime()
    {
        LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
        return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
    }

    public UserPreferences setUserPreferences(UserPreferences newPreferences)
    {
        String userName = newPreferences.getUserName();

        User user = getUser(userName);

        UserPreferences preference = user.getUserPreferences();

        if (newPreferences.getHighPricePoint() != null)
        {
            preference.setHighPricePoint(newPreferences.getHighPricePoint());
        }
        if (newPreferences.getLowerPricePoint() != null)
        {
            preference.setLowerPricePoint(newPreferences.getLowerPricePoint());
        }
        if(newPreferences.getNumberOfAdults() > 0)
        {
            preference.setNumberOfAdults(newPreferences.getNumberOfAdults());
        }
        if(newPreferences.getNumberOfChildren() > 0)
        {
            preference.setNumberOfChildren(newPreferences.getNumberOfChildren());
        }
        if(newPreferences.getTicketQuantity() > 0)
        {
            preference.setTicketQuantity(newPreferences.getTicketQuantity());
        }
        if (newPreferences.getTripDuration() > 0)
        {
            preference.setTripDuration(newPreferences.getTripDuration());
        }
        return preference;
    }
}