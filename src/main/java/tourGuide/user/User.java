package tourGuide.user;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import tripPricer.Provider;

public class User
{
    private final UUID userId;
    private final String userName;
    private String phoneNumber;
    private String emailAddress;
    private Date latestLocationTimestamp;
    private Deque<VisitedLocation> visitedLocations = new ConcurrentLinkedDeque<>();
    private Map<String,UserReward> userRewards      = new ConcurrentHashMap<>();

    private UserPreferences userPreferences = new UserPreferences();
    private List<Provider> tripDeals = new ArrayList<>();

    public User(UUID userId, String userName, String phoneNumber, String emailAddress)
    {
        this.userId = userId;
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.emailAddress = emailAddress;
    }

    public UUID getUserId()
    {
        return userId;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setPhoneNumber(String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber()
    {
        return phoneNumber;
    }

    public void setEmailAddress(String emailAddress)
    {
        this.emailAddress = emailAddress;
    }

    public String getEmailAddress()
    {
        return emailAddress;
    }

    public void setLatestLocationTimestamp(Date latestLocationTimestamp)
    {
        this.latestLocationTimestamp = latestLocationTimestamp;
    }

    public Date getLatestLocationTimestamp()
    {
        return latestLocationTimestamp;
    }

    public void addToVisitedLocations(VisitedLocation visitedLocation)
    {
        visitedLocations.add(visitedLocation);
    }

    public Deque<VisitedLocation> getVisitedLocations()
    {
        return visitedLocations;
    }

    public void clearVisitedLocations()
    {
        visitedLocations.clear();
    }

    public void addUserReward(UserReward userReward)
    {
        userRewards.putIfAbsent(userReward.getAttractionName(), userReward);
    }

    public Collection<UserReward> getUserRewards()
    {
        return userRewards.values();
    }

    public UserPreferences getUserPreferences()
    {
        return userPreferences;
    }

    public void setUserPreferences(UserPreferences userPreferences)
    {
        this.userPreferences = userPreferences;
    }

    public VisitedLocation getLastVisitedLocation()
    {
        return visitedLocations.getFirst();
    }

    public Location getCurrentLocation()
    {
        return getLastVisitedLocation().location;
    }

    public void setTripDeals(List<Provider> tripDeals)
    {
        this.tripDeals = tripDeals;
    }

    public List<Provider> getTripDeals()
    {
        return tripDeals;
    }

    public boolean isAttractionRewarded(Attraction attraction)
    {
        return getUserRewards().stream()
                               .map(UserReward::getAttractionName)
                               .anyMatch(name -> name.equals(attraction.attractionName));
    }
}