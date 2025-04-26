using Microsoft.EntityFrameworkCore;
using WebApplication1.Models;
using Xunit;

namespace TestProject1
{
    public class LocationTest
    {
        private readonly DbContextOptions<GoIn2Context> _options;

        public LocationTest()
        {
            _options = new DbContextOptionsBuilder<GoIn2Context>()
                .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString()) // fresh database per test class
                .Options;
        }

        [Fact]
        public async Task CreateLocation_ShouldWork()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var user = new User
                {
                    FirstName = "Location",
                    LastName = "User",
                    UserType = "student"
                };
                context.Users.Add(user);
                await context.SaveChangesAsync();

                var location = new Location
                {
                    Userid = user.Id,
                    Latitude = 40.1234,
                    Longitude = -79.9876,
                    LocAccuracy = 5,
                    LocAltitude = 100,
                    LocSpeed = 1,
                    LocBearing = 180,
                    LocProvider = "GPS",
                    TimestampMs = 123456789
                };

                // Act
                context.Locations.Add(location);
                await context.SaveChangesAsync();

                // Assert
                var createdLocation = await context.Locations.FindAsync(location.Id);
                Assert.NotNull(createdLocation);
                Assert.Equal(user.Id, createdLocation.Userid);
                Assert.Equal(40.1234, createdLocation.Latitude);
                Assert.Equal(-79.9876, createdLocation.Longitude);
                Assert.Equal("GPS", createdLocation.LocProvider);
            }
        }

        [Fact]
        public async Task GetLocationById_ShouldReturnCorrectLocation()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var user = new User
                {
                    FirstName = "Location",
                    LastName = "Getter",
                    UserType = "student"
                };
                context.Users.Add(user);
                await context.SaveChangesAsync();

                var location = new Location
                {
                    Userid = user.Id,
                    Latitude = 41.0000,
                    Longitude = -80.0000,
                    LocAccuracy = 10,
                    LocAltitude = 200,
                    LocSpeed = 2,
                    LocBearing = 90,
                    LocProvider = "Network",
                    TimestampMs = 987654321
                };
                context.Locations.Add(location);
                await context.SaveChangesAsync();

                // Act
                var foundLocation = await context.Locations.FindAsync(location.Id);

                // Assert
                Assert.NotNull(foundLocation);
                Assert.Equal(41.0000, foundLocation.Latitude);
                Assert.Equal(-80.0000, foundLocation.Longitude);
                Assert.Equal("Network", foundLocation.LocProvider);
            }
        }

        [Fact]
        public async Task GetAllLocations_ShouldReturnAllLocations()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var user = new User
                {
                    FirstName = "MultiLocation",
                    LastName = "User",
                    UserType = "student"
                };
                context.Users.Add(user);
                await context.SaveChangesAsync();

                var locations = new List<Location>
                {
                    new Location { Userid = user.Id, Latitude = 39.1234, Longitude = -78.9876, LocProvider = "GPS", TimestampMs = 111 },
                    new Location { Userid = user.Id, Latitude = 42.1234, Longitude = -81.9876, LocProvider = "Network", TimestampMs = 222 }
                };
                context.Locations.AddRange(locations);
                await context.SaveChangesAsync();

                // Act
                var locationList = await context.Locations.ToListAsync();

                // Assert
                Assert.Equal(2, locationList.Count);
                Assert.Contains(locationList, l => l.Latitude == 39.1234);
                Assert.Contains(locationList, l => l.Latitude == 42.1234);
            }
        }

        [Fact]
        public async Task UpdateLocation_ShouldWork()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var user = new User
                {
                    FirstName = "UpdateLocation",
                    LastName = "User",
                    UserType = "student"
                };
                context.Users.Add(user);
                await context.SaveChangesAsync();

                var location = new Location
                {
                    Userid = user.Id,
                    Latitude = 37.0000,
                    Longitude = -77.0000,
                    LocProvider = "GPS",
                    TimestampMs = 333
                };
                context.Locations.Add(location);
                await context.SaveChangesAsync();

                // Act
                location.Latitude = 37.5000;
                location.LocProvider = "UpdatedProvider";
                context.Locations.Update(location);
                await context.SaveChangesAsync();

                // Assert
                var updatedLocation = await context.Locations.FindAsync(location.Id);
                Assert.Equal(37.5000, updatedLocation.Latitude);
                Assert.Equal("UpdatedProvider", updatedLocation.LocProvider);
            }
        }

        [Fact]
        public async Task DeleteLocation_ShouldWork()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var user = new User
                {
                    FirstName = "DeleteLocation",
                    LastName = "User",
                    UserType = "student"
                };
                context.Users.Add(user);
                await context.SaveChangesAsync();

                var location = new Location
                {
                    Userid = user.Id,
                    Latitude = 36.1234,
                    Longitude = -76.9876,
                    LocProvider = "GPS",
                    TimestampMs = 444
                };
                context.Locations.Add(location);
                await context.SaveChangesAsync();

                // Act
                context.Locations.Remove(location);
                await context.SaveChangesAsync();

                // Assert
                var deletedLocation = await context.Locations.FindAsync(location.Id);
                Assert.Null(deletedLocation);
            }
        }
    }
}
