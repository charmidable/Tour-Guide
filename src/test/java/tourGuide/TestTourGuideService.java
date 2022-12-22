package tourGuide;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.*;

import gpsUtil.location.Location;
import org.javamoney.moneta.Money;
import org.junit.Before;
import org.junit.Test;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;
import tripPricer.Provider;

import javax.money.Monetary;

public class TestTourGuideService
{
	@Before
	public void setUp()
	{
		Locale.setDefault(Locale.US);
	}


	@Test
	public void getUserLocation()
	{
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

		VisitedLocation visitedLocation = tourGuideService.getUserLocation(user);

		tourGuideService.tracker.stopTracking();
		assertTrue(visitedLocation.userId.equals(user.getUserId()));
	}


	@Test
	public void addUser()
	{
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");
		tourGuideService.addUser(user);
		tourGuideService.addUser(user2);
		User retrivedUser = tourGuideService.getUser(user.getUserName());
		User retrivedUser2 = tourGuideService.getUser(user2.getUserName());
		tourGuideService.tracker.stopTracking();
		assertEquals(user, retrivedUser);
		assertEquals(user2, retrivedUser2);
	}


	@Test
	public void getAllUsers()
	{
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		User user1 = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");
		tourGuideService.addUser(user1);
		tourGuideService.addUser(user2);
		List<User> allUsers = tourGuideService.getAllUsers();
		tourGuideService.tracker.stopTracking();
		assertTrue(allUsers.contains(user1));
		assertTrue(allUsers.contains(user2));
	}


	@Test
	public void trackUserLocation()
	{
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);
		tourGuideService.tracker.stopTracking();
		assertEquals(user.getUserId(), visitedLocation.userId);
	}



	@Test
	public void getNearbyAttractions()
	{
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);
		List<Attraction> attractions = tourGuideService.getNearbyAttractions(visitedLocation);
		tourGuideService.tracker.stopTracking();
		assertEquals(5, attractions.size());
	}


	@Test
	public void getTripDeals()
	{
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		List<Provider> providers = tourGuideService.getTripDeals(user);
		tourGuideService.tracker.stopTracking();
		assertEquals(5, providers.size());
	}



	@Test
	public void getAllVisitedLocationsOfAllUsers()
	{
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

		User user1 = new User(UUID.randomUUID(), "jon1", "111", "jon1@tourGuide.com");
		user1.addToVisitedLocations(new VisitedLocation(user1.getUserId(),new Location(1,1), new Date()));
		user1.addToVisitedLocations(new VisitedLocation(user1.getUserId(),new Location(11,11), new Date()));
		user1.addToVisitedLocations(new VisitedLocation(user1.getUserId(),new Location(12,12), new Date()));
		tourGuideService.addUser(user1);

		User user2 = new User(UUID.randomUUID(), "jon2", "222", "jon2@tourGuide.com");
		user2.addToVisitedLocations(new VisitedLocation(user2.getUserId(), new Location(2,2), new Date()));
		user2.addToVisitedLocations(new VisitedLocation(user2.getUserId(),new Location(21,21), new Date()));
		user2.addToVisitedLocations(new VisitedLocation(user2.getUserId(),new Location(22,22), new Date()));
		tourGuideService.addUser(user2);

		User user3 = new User(UUID.randomUUID(), "jon3", "333", "jon3@tourGuide.com");
		user3.addToVisitedLocations(new VisitedLocation(user3.getUserId(),new Location(3,3), new Date()));
		user3.addToVisitedLocations(new VisitedLocation(user3.getUserId(),new Location(31,31), new Date()));
		user3.addToVisitedLocations(new VisitedLocation(user3.getUserId(),new Location(32,32), new Date()));
		tourGuideService.addUser(user3);

		List<VisitedLocation> allLocationsOfAllUsers = tourGuideService.getAllVisitedLocationsOfAllUsers();

		assertEquals(9, allLocationsOfAllUsers.size());

		assertTrue(allLocationsOfAllUsers.containsAll(user1.getVisitedLocations()));
		assertTrue(allLocationsOfAllUsers.containsAll(user2.getVisitedLocations()));
		assertTrue(allLocationsOfAllUsers.containsAll(user3.getVisitedLocations()));
	}

	@Test
	public void updateUserPreferencesTest()
	{
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

		User user =new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		tourGuideService.addUser(user);

		UserPreferences userPreferences = new UserPreferences();
		userPreferences.setNumberOfAdults(1);
		userPreferences.setNumberOfChildren(1);
		userPreferences.setTripDuration(1);
		userPreferences.setTicketQuantity(1);
		userPreferences.setLowerPricePoint(Money.of(1, Monetary.getCurrency("USD")));
		userPreferences.setHighPricePoint(Money.of(1, Monetary.getCurrency("USD")));
		userPreferences.setAttractionProximity(1);

		user.setUserPreferences(userPreferences);

		assertEquals(user.getUserPreferences().getNumberOfAdults(), 1);
		assertEquals(user.getUserPreferences().getNumberOfChildren(), 1);
		assertEquals(user.getUserPreferences().getTripDuration(), 1);
		assertEquals(user.getUserPreferences().getTicketQuantity(), 1);
		assertEquals(user.getUserPreferences().getLowerPricePoint(), Money.of(1, Monetary.getCurrency("USD")));
		assertEquals(user.getUserPreferences().getHighPricePoint(), Money.of(1, Monetary.getCurrency("USD")));
		assertEquals(user.getUserPreferences().getAttractionProximity(), 1);
	}
}