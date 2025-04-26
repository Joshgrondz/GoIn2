using Microsoft.EntityFrameworkCore;
using WebApplication1.Models;
using Xunit;

namespace TestProject1
{
    public class LogTest
    {
        private readonly DbContextOptions<GoIn2Context> _options;

        public LogTest()
        {
            _options = new DbContextOptionsBuilder<GoIn2Context>()
                .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString()) // fresh database per test class
                .Options;
        }

        [Fact]
        public async Task CreateLog_ShouldWork()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var eventEntity = new Event
                {
                    EventName = "Test Event",
                    EventDate = DateOnly.FromDateTime(DateTime.UtcNow),
                    EventLocation = "Test Location",
                    Status = true
                };
                context.Events.Add(eventEntity);
                await context.SaveChangesAsync();

                var log = new Log
                {
                    Eventid = eventEntity.Id,
                    LogDescription = "This is a test log entry",
                    Timestamp = DateTime.UtcNow
                };

                // Act
                context.Logs.Add(log);
                await context.SaveChangesAsync();

                // Assert
                var createdLog = await context.Logs.FindAsync(log.Id);
                Assert.NotNull(createdLog);
                Assert.Equal("This is a test log entry", createdLog.LogDescription);
                Assert.Equal(eventEntity.Id, createdLog.Eventid);
            }
        }

        [Fact]
        public async Task GetLogById_ShouldReturnCorrectLog()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var eventEntity = new Event
                {
                    EventName = "Getter Event",
                    EventDate = DateOnly.FromDateTime(DateTime.UtcNow),
                    EventLocation = "Somewhere",
                    Status = true
                };
                context.Events.Add(eventEntity);
                await context.SaveChangesAsync();

                var log = new Log
                {
                    Eventid = eventEntity.Id,
                    LogDescription = "Fetching this log",
                    Timestamp = DateTime.UtcNow
                };
                context.Logs.Add(log);
                await context.SaveChangesAsync();

                // Act
                var foundLog = await context.Logs.FindAsync(log.Id);

                // Assert
                Assert.NotNull(foundLog);
                Assert.Equal("Fetching this log", foundLog.LogDescription);
            }
        }

        [Fact]
        public async Task GetAllLogs_ShouldReturnAllLogs()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var eventEntity = new Event
                {
                    EventName = "Multi Event",
                    EventDate = DateOnly.FromDateTime(DateTime.UtcNow),
                    EventLocation = "MultiLocation",
                    Status = true
                };
                context.Events.Add(eventEntity);
                await context.SaveChangesAsync();

                var logs = new List<Log>
                {
                    new Log { Eventid = eventEntity.Id, LogDescription = "First Log", Timestamp = DateTime.UtcNow },
                    new Log { Eventid = eventEntity.Id, LogDescription = "Second Log", Timestamp = DateTime.UtcNow }
                };
                context.Logs.AddRange(logs);
                await context.SaveChangesAsync();

                // Act
                var logList = await context.Logs.ToListAsync();

                // Assert
                Assert.Equal(2, logList.Count);
                Assert.Contains(logList, l => l.LogDescription == "First Log");
                Assert.Contains(logList, l => l.LogDescription == "Second Log");
            }
        }

        [Fact]
        public async Task UpdateLog_ShouldWork()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var eventEntity = new Event
                {
                    EventName = "Update Event",
                    EventDate = DateOnly.FromDateTime(DateTime.UtcNow),
                    EventLocation = "UpdateLocation",
                    Status = true
                };
                context.Events.Add(eventEntity);
                await context.SaveChangesAsync();

                var log = new Log
                {
                    Eventid = eventEntity.Id,
                    LogDescription = "Old Log Description",
                    Timestamp = DateTime.UtcNow
                };
                context.Logs.Add(log);
                await context.SaveChangesAsync();

                // Act
                log.LogDescription = "Updated Log Description";
            }
        }
    }
}