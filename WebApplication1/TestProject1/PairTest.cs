using Microsoft.EntityFrameworkCore;
using WebApplication1.Models;
using Xunit;

namespace TestProject1
{
    public class PairTest
    {
        private readonly DbContextOptions<GoIn2Context> _options;

        public PairTest()
        {
            _options = new DbContextOptionsBuilder<GoIn2Context>()
                .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString()) // fresh database per test class
                .Options;
        }

        [Fact]
        public async Task CreatePair_ShouldWork()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var student1 = new User { FirstName = "Student", LastName = "One", UserType = "student" };
                var student2 = new User { FirstName = "Student", LastName = "Two", UserType = "student" };
                context.Users.AddRange(student1, student2);
                await context.SaveChangesAsync();

                var eventEntity = new Event
                {
                    EventName = "Pair Event",
                    EventDate = DateOnly.FromDateTime(DateTime.UtcNow),
                    EventLocation = "Park",
                    Status = true
                };
                context.Events.Add(eventEntity);
                await context.SaveChangesAsync();

                var pair = new Pair
                {
                    Student1id = student1.Id,
                    Student2id = student2.Id,
                    Eventid = eventEntity.Id,
                    Status = true // ✅ Correct: Status is bool
                };

                // Act
                context.Pairs.Add(pair);
                await context.SaveChangesAsync();

                // Assert
                var createdPair = await context.Pairs.FindAsync(pair.Id);
                Assert.NotNull(createdPair);
                Assert.Equal(student1.Id, createdPair.Student1id);
                Assert.Equal(student2.Id, createdPair.Student2id);
                Assert.True(createdPair.Status);
            }
        }

        [Fact]
        public async Task GetPairById_ShouldReturnCorrectPair()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var student1 = new User { FirstName = "First", LastName = "Student", UserType = "student" };
                var student2 = new User { FirstName = "Second", LastName = "Student", UserType = "student" };
                context.Users.AddRange(student1, student2);
                await context.SaveChangesAsync();

                var eventEntity = new Event
                {
                    EventName = "Find Pair Event",
                    EventDate = DateOnly.FromDateTime(DateTime.UtcNow),
                    EventLocation = "Gym",
                    Status = true
                };
                context.Events.Add(eventEntity);
                await context.SaveChangesAsync();

                var pair = new Pair
                {
                    Student1id = student1.Id,
                    Student2id = student2.Id,
                    Eventid = eventEntity.Id,
                    Status = false // ✅ Correct
                };
                context.Pairs.Add(pair);
                await context.SaveChangesAsync();

                // Act
                var foundPair = await context.Pairs.FindAsync(pair.Id);

                // Assert
                Assert.NotNull(foundPair);
                Assert.False(foundPair.Status);
            }
        }

        [Fact]
        public async Task GetAllPairs_ShouldReturnAllPairs()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var student1 = new User { FirstName = "A", LastName = "One", UserType = "student" };
                var student2 = new User { FirstName = "B", LastName = "Two", UserType = "student" };
                var student3 = new User { FirstName = "C", LastName = "Three", UserType = "student" };
                var student4 = new User { FirstName = "D", LastName = "Four", UserType = "student" };

                context.Users.AddRange(student1, student2, student3, student4);
                await context.SaveChangesAsync();

                var eventEntity = new Event
                {
                    EventName = "Multi Pairs Event",
                    EventDate = DateOnly.FromDateTime(DateTime.UtcNow),
                    EventLocation = "School",
                    Status = true
                };
                context.Events.Add(eventEntity);
                await context.SaveChangesAsync();

                var pairs = new List<Pair>
                {
                    new Pair { Student1id = student1.Id, Student2id = student2.Id, Eventid = eventEntity.Id, Status = true },
                    new Pair { Student1id = student3.Id, Student2id = student4.Id, Eventid = eventEntity.Id, Status = false }
                };
                context.Pairs.AddRange(pairs);
                await context.SaveChangesAsync();

                // Act
                var pairList = await context.Pairs.ToListAsync();

                // Assert
                Assert.Equal(2, pairList.Count);
                Assert.Contains(pairList, p => p.Status == true);
                Assert.Contains(pairList, p => p.Status == false);
            }
        }

        [Fact]
        public async Task UpdatePair_ShouldWork()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var student1 = new User { FirstName = "Update", LastName = "One", UserType = "student" };
                var student2 = new User { FirstName = "Update", LastName = "Two", UserType = "student" };
                context.Users.AddRange(student1, student2);
                await context.SaveChangesAsync();

                var eventEntity = new Event
                {
                    EventName = "Update Pair Event",
                    EventDate = DateOnly.FromDateTime(DateTime.UtcNow),
                    EventLocation = "Update Gym",
                    Status = true
                };
                context.Events.Add(eventEntity);
                await context.SaveChangesAsync();

                var pair = new Pair
                {
                    Student1id = student1.Id,
                    Student2id = student2.Id,
                    Eventid = eventEntity.Id,
                    Status = false // initially false
                };
                context.Pairs.Add(pair);
                await context.SaveChangesAsync();

                // Act
                pair.Status = true; // ✅ updating to true
                context.Pairs.Update(pair);
                await context.SaveChangesAsync();

                // Assert
                var updatedPair = await context.Pairs.FindAsync(pair.Id);
                Assert.True(updatedPair.Status);
            }
        }

        [Fact]
        public async Task DeletePair_ShouldWork()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var student1 = new User { FirstName = "Delete", LastName = "One", UserType = "student" };
                var student2 = new User { FirstName = "Delete", LastName = "Two", UserType = "student" };
                context.Users.AddRange(student1, student2);
                await context.SaveChangesAsync();

                var eventEntity = new Event
                {
                    EventName = "Delete Pair Event",
                    EventDate = DateOnly.FromDateTime(DateTime.UtcNow),
                    EventLocation = "Delete Gym",
                    Status = true
                };
                context.Events.Add(eventEntity);
                await context.SaveChangesAsync();

                var pair = new Pair
                {
                    Student1id = student1.Id,
                    Student2id = student2.Id,
                    Eventid = eventEntity.Id,
                    Status = false
                };
                context.Pairs.Add(pair);
                await context.SaveChangesAsync();

                // Act
                context.Pairs.Remove(pair);
                await context.SaveChangesAsync();

                // Assert
                var deletedPair = await context.Pairs.FindAsync(pair.Id);
                Assert.Null(deletedPair);
            }
        }
    }
}
