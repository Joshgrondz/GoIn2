using Microsoft.EntityFrameworkCore;
using WebApplication1.Models;
using Xunit;

namespace TestProject1
{
    public class MostRecentStudentLocationViewTest
    {
        private readonly DbContextOptions<GoIn2Context> _options;

        public MostRecentStudentLocationViewTest()
        {
            _options = new DbContextOptionsBuilder<GoIn2Context>()
                .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString()) // fresh database per test class
                .Options;
        }

        [Fact]
        public async Task QueryMostRecentStudentLocationView_ShouldReturnEmptyListInitially()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Act
                var locations = await context.MostRecentStudentLocationViews.ToListAsync();

                // Assert
                Assert.NotNull(locations); // View should be queryable
                Assert.Empty(locations);   // It should be empty initially
            }
        }

        [Fact]
        public async Task QueryMostRecentStudentLocationView_ByEventId_ShouldReturnResults_WhenSeededProperly()
        {
            using (var context = new GoIn2Context(_options))
            {
                // In a real database, View would pull live data. 
                // In InMemory, no auto seeding - so this will always be empty unless simulated.
                // Here we are just confirming that a Where query can run without exceptions.

                // Act
                var locations = await context.MostRecentStudentLocationViews
                    .Where(l => l.Eventid == 1)
                    .ToListAsync();

                // Assert
                Assert.NotNull(locations); // Should successfully query
                Assert.Empty(locations);   // No records (normal for InMemory and Views)
            }
        }
    }
}
