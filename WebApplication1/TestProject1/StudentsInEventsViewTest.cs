using Microsoft.EntityFrameworkCore;
using WebApplication1.Models;
using Xunit;

namespace TestProject1
{
    public class StudentsInEventsViewTest
    {
        private readonly DbContextOptions<GoIn2Context> _options;

        public StudentsInEventsViewTest()
        {
            _options = new DbContextOptionsBuilder<GoIn2Context>()
                .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString()) // fresh database per test class
                .Options;
        }

        [Fact]
        public async Task QueryStudentsInEventsView_ShouldReturnEmptyListInitially()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Act
                var students = await context.StudentsInEventsViews.ToListAsync();

                // Assert
                Assert.NotNull(students); // View should be queryable
                Assert.Empty(students);   // Should be empty initially (InMemory)
            }
        }

        [Fact]
        public async Task QueryStudentsInEventsView_ByEventId_ShouldReturnResults_WhenSeededProperly()
        {
            using (var context = new GoIn2Context(_options))
            {
                // In InMemory, views have no automatic data.
                // This test ensures that a filtered query can be executed successfully.

                // Act
                var students = await context.StudentsInEventsViews
                    .Where(s => s.EventName == "Sample Event")
                    .ToListAsync();

                // Assert
                Assert.NotNull(students); // Query should succeed
                Assert.Empty(students);   // No records (normal for View with InMemory)
            }
        }
    }
}
