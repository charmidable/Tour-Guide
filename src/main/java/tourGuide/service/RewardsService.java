package tourGuide.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.user.User;
import tourGuide.user.UserReward;

@Service
public class RewardsService
{
    //=========================
    //=      Attributes       =
    //=========================
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

    // proximity in miles
    private int defaultProximityBuffer = 10;
    private int proximityBuffer = defaultProximityBuffer;
    private int attractionProximityRange = 200;

    private final GpsUtil gpsUtil;
    private final RewardCentral rewardsCentral;


    //=========================
    //=     Constructors      =
    //=========================
    public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral)
    {
        this.gpsUtil = gpsUtil;
        this.rewardsCentral = rewardCentral;
    }

    //=========================
    //=   Business Methods    =
    //=========================


    public void calculateRewards(User user)
    {
        Collection<VisitedLocation> userLocations = user.getVisitedLocations();

        List<Attraction> attractions = gpsUtil.getAttractions();

        for (VisitedLocation visitedLocation : userLocations)
        {
            for (Attraction attraction : attractions)
            {
                if(nearAttraction(visitedLocation, attraction))
                {
                    UserReward userReward = new UserReward(visitedLocation, attraction);
                    user.addUserReward(userReward);
                    applyRewardPoints(userReward);
                }
            }
        }
    }


    public void applyRewardPoints(UserReward userReward)
    {
        CompletableFuture.supplyAsync(() -> getRewardPoints(userReward.attraction))
                         .thenAccept (userReward::setRewardPoints);
    }


    public boolean isWithinAttractionProximity(Attraction attraction, Location location)
    {
        return getDistance(attraction, location) > attractionProximityRange ? false : true;
    }


    private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction)
    {
        return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
    }


    public int getRewardPoints(Attraction attraction)
    {
        return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, null);
    }


    public double getDistance(Location loc1, Location loc2)
    {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
        return statuteMiles;
    }


    //=========================
    //=   Getters & Setters   =
    //=========================
    public void setProximityBuffer(int proximityBuffer)
    {
        this.proximityBuffer = proximityBuffer;
    }

    public void setDefaultProximityBuffer()
    {
        proximityBuffer = defaultProximityBuffer;
    }
}