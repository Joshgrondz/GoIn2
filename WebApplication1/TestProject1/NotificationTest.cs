using Microsoft.EntityFrameworkCore;
using WebApplication1.Models;
using Xunit;

namespace TestProject1
{
    public class NotificationTest
    {
        private readonly DbContextOptions<GoIn2Context> _options;

        public NotificationTest()
        {
            _options = new DbContextOptionsBuilder<GoIn2Context>()
                .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString()) // fresh database per test class
                .Options;
        }

        [Fact]
        public async Task CreateNotification_ShouldWork()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var user = new User
                {
                    FirstName = "Notification",
                    LastName = "User",
                    UserType = "student"
                };
                context.Users.Add(user);
                await context.SaveChangesAsync();

                var notification = new Notification
                {
                    Userid = user.Id,
                    NotificationDescription = "Test Notification",
                    NotificationTimestamp = DateTime.UtcNow,
                    Sent = false
                };

                // Act
                context.Notifications.Add(notification);
                await context.SaveChangesAsync();

                // Assert
                var createdNotification = await context.Notifications.FindAsync(notification.Id);
                Assert.NotNull(createdNotification);
                Assert.Equal(user.Id, createdNotification.Userid);
                Assert.Equal("Test Notification", createdNotification.NotificationDescription);
                Assert.False(createdNotification.Sent);
            }
        }

        [Fact]
        public async Task GetNotificationById_ShouldReturnCorrectNotification()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var user = new User
                {
                    FirstName = "Notif",
                    LastName = "Getter",
                    UserType = "student"
                };
                context.Users.Add(user);
                await context.SaveChangesAsync();

                var notification = new Notification
                {
                    Userid = user.Id,
                    NotificationDescription = "Get This Notification",
                    NotificationTimestamp = DateTime.UtcNow,
                    Sent = true
                };
                context.Notifications.Add(notification);
                await context.SaveChangesAsync();

                // Act
                var foundNotification = await context.Notifications.FindAsync(notification.Id);

                // Assert
                Assert.NotNull(foundNotification);
                Assert.Equal("Get This Notification", foundNotification.NotificationDescription);
                Assert.True(foundNotification.Sent);
            }
        }

        [Fact]
        public async Task GetAllNotifications_ShouldReturnAllNotifications()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var user = new User
                {
                    FirstName = "MultiNotif",
                    LastName = "User",
                    UserType = "student"
                };
                context.Users.Add(user);
                await context.SaveChangesAsync();

                var notifications = new List<Notification>
                {
                    new Notification { Userid = user.Id, NotificationDescription = "First Notif", NotificationTimestamp = DateTime.UtcNow, Sent = false },
                    new Notification { Userid = user.Id, NotificationDescription = "Second Notif", NotificationTimestamp = DateTime.UtcNow, Sent = true }
                };
                context.Notifications.AddRange(notifications);
                await context.SaveChangesAsync();

                // Act
                var notificationList = await context.Notifications.ToListAsync();

                // Assert
                Assert.Equal(2, notificationList.Count);
                Assert.Contains(notificationList, n => n.NotificationDescription == "First Notif");
                Assert.Contains(notificationList, n => n.NotificationDescription == "Second Notif");
            }
        }

        [Fact]
        public async Task UpdateNotification_ShouldWork()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var user = new User
                {
                    FirstName = "UpdateNotif",
                    LastName = "User",
                    UserType = "student"
                };
                context.Users.Add(user);
                await context.SaveChangesAsync();

                var notification = new Notification
                {
                    Userid = user.Id,
                    NotificationDescription = "Old Description",
                    NotificationTimestamp = DateTime.UtcNow,
                    Sent = false
                };
                context.Notifications.Add(notification);
                await context.SaveChangesAsync();

                // Act
                notification.NotificationDescription = "Updated Description";
                notification.Sent = true;
                context.Notifications.Update(notification);
                await context.SaveChangesAsync();

                // Assert
                var updatedNotification = await context.Notifications.FindAsync(notification.Id);
                Assert.Equal("Updated Description", updatedNotification.NotificationDescription);
                Assert.True(updatedNotification.Sent);
            }
        }

        [Fact]
        public async Task DeleteNotification_ShouldWork()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var user = new User
                {
                    FirstName = "DeleteNotif",
                    LastName = "User",
                    UserType = "student"
                };
                context.Users.Add(user);
                await context.SaveChangesAsync();

                var notification = new Notification
                {
                    Userid = user.Id,
                    NotificationDescription = "Delete This",
                    NotificationTimestamp = DateTime.UtcNow,
                    Sent = false
                };
                context.Notifications.Add(notification);
                await context.SaveChangesAsync();

                // Act
                context.Notifications.Remove(notification);
                await context.SaveChangesAsync();

                // Assert
                var deletedNotification = await context.Notifications.FindAsync(notification.Id);
                Assert.Null(deletedNotification);
            }
        }
    }
}
