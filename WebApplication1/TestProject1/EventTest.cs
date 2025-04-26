using Microsoft.EntityFrameworkCore;
using WebApplication1.Models;
using Xunit;

namespace TestProject1
{
    public class EventTest
    {
        private readonly DbContextOptions<GoIn2Context> _options;

        public EventTest()
        {
            _options = new DbContextOptionsBuilder<GoIn2Context>()
                .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString()) // fresh database per test class
                .Options;
        }

        [Fact]
        public async Task CreateEvent_ShouldWork()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var teacher = new User
                {
                    FirstName = "Event",
                    LastName = "Teacher",
                    UserType = "teacher"
                };
                context.Users.Add(teacher);
                await context.SaveChangesAsync();

                var eventEntity = new Event
                {
                    EventName = "Field Trip",
                    EventDate = DateOnly.FromDateTime(DateTime.UtcNow),
                    EventLocation = "Museum",
                    Status = true,
                    Teacherid = teacher.Id
                };


                // Act
                context.Events.Add(eventEntity);
                await context.SaveChangesAsync();

                // Assert
                var createdEvent = await context.Events.FindAsync(eventEntity.Id);
                Assert.NotNull(createdEvent);
                Assert.Equal("Field Trip", createdEvent.EventName);
                Assert.Equal("Museum", createdEvent.EventLocation);
                Assert.True(createdEvent.Status);
                Assert.Equal(teacher.Id, createdEvent.Teacherid);
            }
        }

        [Fact]
        public async Task GetEventById_ShouldReturnCorrectEvent()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var teacher = new User
                {
                    FirstName = "Event",
                    LastName = "Getter",
                    UserType = "teacher"
                };
                context.Users.Add(teacher);
                await context.SaveChangesAsync();

                var eventEntity = new Event
                {
                    EventName = "Science Fair",
                    EventDate = DateOnly.FromDateTime(DateTime.UtcNow),
                    EventLocation = "Gym",
                    Status = true,
                    Teacherid = teacher.Id
                };
                context.Events.Add(eventEntity);
                await context.SaveChangesAsync();

                // Act
                var foundEvent = await context.Events.FindAsync(eventEntity.Id);

                // Assert
                Assert.NotNull(foundEvent);
                Assert.Equal("Science Fair", foundEvent.EventName);
                Assert.Equal("Gym", foundEvent.EventLocation);
            }
        }

        [Fact]
        public async Task GetAllEvents_ShouldReturnAllEvents()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var teacher = new User
                {
                    FirstName = "Teacher",
                    LastName = "MultipleEvents",
                    UserType = "teacher"
                };
                context.Users.Add(teacher);
                await context.SaveChangesAsync();

                var events = new List<Event>
                {
                    new Event { EventName = "Concert", EventDate = DateOnly.FromDateTime(DateTime.UtcNow), EventLocation = "Auditorium", Status = true, Teacherid = teacher.Id },
                    new Event { EventName = "Basketball Game", EventDate = DateOnly.FromDateTime(DateTime.UtcNow), EventLocation = "Arena", Status = false, Teacherid = teacher.Id }
                };
                context.Events.AddRange(events);
                await context.SaveChangesAsync();

                // Act
                var eventList = await context.Events.ToListAsync();

                // Assert
                Assert.Equal(2, eventList.Count);
                Assert.Contains(eventList, e => e.EventName == "Concert");
                Assert.Contains(eventList, e => e.EventName == "Basketball Game");
            }
        }

        [Fact]
        public async Task UpdateEvent_ShouldWork()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var teacher = new User
                {
                    FirstName = "Event",
                    LastName = "Updater",
                    UserType = "teacher"
                };
                context.Users.Add(teacher);
                await context.SaveChangesAsync();

                var eventEntity = new Event
                {
                    EventName = "Original Event",
                    EventDate = DateOnly.FromDateTime(DateTime.UtcNow),
                    EventLocation = "Old Location",
                    Status = true,
                    Teacherid = teacher.Id
                };
                context.Events.Add(eventEntity);
                await context.SaveChangesAsync();

                // Act
                eventEntity.EventLocation = "New Location";
                eventEntity.Status = false;
                context.Events.Update(eventEntity);
                await context.SaveChangesAsync();

                // Assert
                var updatedEvent = await context.Events.FindAsync(eventEntity.Id);
                Assert.Equal("New Location", updatedEvent.EventLocation);
                Assert.False(updatedEvent.Status);
            }
        }

        [Fact]
        public async Task DeleteEvent_ShouldWork()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var teacher = new User
                {
                    FirstName = "Event",
                    LastName = "Deleter",
                    UserType = "teacher"
                };
                context.Users.Add(teacher);
                await context.SaveChangesAsync();

                var eventEntity = new Event
                {
                    EventName = "Delete Me",
                    EventDate = DateOnly.FromDateTime(DateTime.UtcNow),
                    EventLocation = "Delete Location",
                    Status = true,
                    Teacherid = teacher.Id
                };
                context.Events.Add(eventEntity);
                await context.SaveChangesAsync();

                // Act
                context.Events.Remove(eventEntity);
                await context.SaveChangesAsync();

                // Assert
                var deletedEvent = await context.Events.FindAsync(eventEntity.Id);
                Assert.Null(deletedEvent);
            }
        }
    }
}
