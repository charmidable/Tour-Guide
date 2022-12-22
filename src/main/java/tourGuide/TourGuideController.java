package tourGuide;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.jsoniter.output.JsonStream;

import gpsUtil.location.VisitedLocation;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;
import tripPricer.Provider;


@RestController
public class TourGuideController
{
    private final  TourGuideService tourGuideService;

    public TourGuideController(TourGuideService tourGuideService)
    {
        this.tourGuideService = tourGuideService;
    }


    @RequestMapping("/")
    public String index()
    {
        return "Greetings from TourGuide!";
    }


    @RequestMapping("/getLocation")
    public String getLocation(@RequestParam String userName)
    {
        VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
        return JsonStream.serialize(visitedLocation.location);
    }


    @RequestMapping("/getNearbyAttractions")
    public String getNearbyAttractions(@RequestParam String userName)
    {
        VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
        return JsonStream.serialize(tourGuideService.getNearbyAttractions(visitedLocation));
    }


    @RequestMapping("/getRewards")
    public String getRewards(@RequestParam String userName)
    {
        return JsonStream.serialize(tourGuideService.getUserRewards(getUser(userName)));
    }


    @RequestMapping("/getAllLocationsOfAllUsers")
    public String getAllLocationsOfAllUsers()
    {
        return JsonStream.serialize(tourGuideService.getAllVisitedLocationsOfAllUsers());
    }


    @RequestMapping("/getTripDeals")
    public String getTripDeals(@RequestParam String userName)
    {
        List<Provider> providers = tourGuideService.getTripDeals(getUser(userName));
        return JsonStream.serialize(providers);
    }


    @PostMapping("/updateUserPreferences")
    public String setUserPreferences(@RequestBody UserPreferences preferences)
    {
        UserPreferences userPreferences = tourGuideService.setUserPreferences(preferences);
        return JsonStream.serialize(userPreferences);
    }


    private User getUser(String userName)
    {
        return tourGuideService.getUser(userName);
    }
}