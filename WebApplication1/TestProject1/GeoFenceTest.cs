using Microsoft.EntityFrameworkCore;
using WebApplication1.Models;
using Xunit;

namespace TestProject1
{
    public class GeoFenceTest
    {
        private readonly DbContextOptions<GoIn2Context> _options;

        public GeoFenceTest()
        {
            _options = new DbContextOptionsBuilder<GoIn2Context>()
                .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString()) // fresh database per test class
                .Options;
        }

        [Fact]
        public async Task CreateGeoFence_ShouldWork()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var geoFence = new GeoFence
                {
                    EventRadius = 100,
                    TeacherRadius = 50,
                    PairDistance = 10,
                    Latitude = 40.1234,
                    Longitude = -79.9876
                };

                // Act
                context.GeoFences.Add(geoFence);
                await context.SaveChangesAsync();

                // Assert
                var createdGeoFence = await context.GeoFences.FindAsync(geoFence.Id);
                Assert.NotNull(createdGeoFence);
                Assert.Equal(100, createdGeoFence.EventRadius);
                Assert.Equal(50, createdGeoFence.TeacherRadius);
                Assert.Equal(10, createdGeoFence.PairDistance);
                Assert.Equal(40.1234, createdGeoFence.Latitude);
                Assert.Equal(-79.9876, createdGeoFence.Longitude);
            }
        }

        [Fact]
        public async Task GetGeoFenceById_ShouldReturnCorrectGeoFence()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var geoFence = new GeoFence
                {
                    EventRadius = 200,
                    TeacherRadius = 75,
                    PairDistance = 20,
                    Latitude = 41.0000,
                    Longitude = -80.0000
                };
                context.GeoFences.Add(geoFence);
                await context.SaveChangesAsync();

                // Act
                var foundGeoFence = await context.GeoFences.FindAsync(geoFence.Id);

                // Assert
                Assert.NotNull(foundGeoFence);
                Assert.Equal(200, foundGeoFence.EventRadius);
                Assert.Equal(75, foundGeoFence.TeacherRadius);
                Assert.Equal(20, foundGeoFence.PairDistance);
            }
        }

        [Fact]
        public async Task GetAllGeoFences_ShouldReturnAllGeoFences()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var geoFence1 = new GeoFence
                {
                    EventRadius = 150,
                    TeacherRadius = 60,
                    PairDistance = 15,
                    Latitude = 39.1234,
                    Longitude = -78.9876
                };

                var geoFence2 = new GeoFence
                {
                    EventRadius = 250,
                    TeacherRadius = 90,
                    PairDistance = 25,
                    Latitude = 42.1234,
                    Longitude = -81.9876
                };

                context.GeoFences.AddRange(geoFence1, geoFence2);
                await context.SaveChangesAsync();

                // Act
                var geoFenceList = await context.GeoFences.ToListAsync();

                // Assert
                Assert.Equal(2, geoFenceList.Count);
                Assert.Contains(geoFenceList, g => g.EventRadius == 150);
                Assert.Contains(geoFenceList, g => g.EventRadius == 250);
            }
        }

        [Fact]
        public async Task UpdateGeoFence_ShouldWork()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var geoFence = new GeoFence
                {
                    EventRadius = 300,
                    TeacherRadius = 100,
                    PairDistance = 30,
                    Latitude = 38.0000,
                    Longitude = -77.0000
                };
                context.GeoFences.Add(geoFence);
                await context.SaveChangesAsync();

                // Act
                geoFence.EventRadius = 400;
                geoFence.Latitude = 38.5000;
                context.GeoFences.Update(geoFence);
                await context.SaveChangesAsync();

                // Assert
                var updatedGeoFence = await context.GeoFences.FindAsync(geoFence.Id);
                Assert.Equal(400, updatedGeoFence.EventRadius);
                Assert.Equal(38.5000, updatedGeoFence.Latitude);
            }
        }

        [Fact]
        public async Task DeleteGeoFence_ShouldWork()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var geoFence = new GeoFence
                {
                    EventRadius = 500,
                    TeacherRadius = 120,
                    PairDistance = 40,
                    Latitude = 37.1234,
                    Longitude = -76.9876
                };
                context.GeoFences.Add(geoFence);
                await context.SaveChangesAsync();

                // Act
                context.GeoFences.Remove(geoFence);
                await context.SaveChangesAsync();

                // Assert
                var deletedGeoFence = await context.GeoFences.FindAsync(geoFence.Id);
                Assert.Null(deletedGeoFence);
            }
        }
    }
}
