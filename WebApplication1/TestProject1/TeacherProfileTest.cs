using Microsoft.EntityFrameworkCore;
using WebApplication1.Models;
using Xunit;

namespace TestProject1
{
    public class TeacherProfileTest
    {
        private readonly DbContextOptions<GoIn2Context> _options;

        public TeacherProfileTest()
        {
            _options = new DbContextOptionsBuilder<GoIn2Context>()
                .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString()) // fresh database per test class
                .Options;
        }

        [Fact]
        public async Task CreateTeacherProfile_ShouldWork()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var user = new User
                {
                    FirstName = "Teacher",
                    LastName = "Example",
                    UserType = "teacher"
                };
                context.Users.Add(user);
                await context.SaveChangesAsync();

                var teacherProfile = new TeacherProfile
                {
                    Id = user.Id
                };

                // Act
                context.TeacherProfiles.Add(teacherProfile);
                await context.SaveChangesAsync();

                // Assert
                var createdProfile = await context.TeacherProfiles.FindAsync(user.Id);
                Assert.NotNull(createdProfile);
                Assert.Equal(user.Id, createdProfile.Id);
            }
        }

        [Fact]
        public async Task GetTeacherProfileById_ShouldReturnCorrectProfile()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var user = new User
                {
                    FirstName = "John",
                    LastName = "Doe",
                    UserType = "teacher"
                };
                context.Users.Add(user);
                await context.SaveChangesAsync();

                var teacherProfile = new TeacherProfile
                {
                    Id = user.Id
                };
                context.TeacherProfiles.Add(teacherProfile);
                await context.SaveChangesAsync();

                // Act
                var foundProfile = await context.TeacherProfiles.FindAsync(user.Id);

                // Assert
                Assert.NotNull(foundProfile);
                Assert.Equal(user.Id, foundProfile.Id);
            }
        }

        [Fact]
        public async Task GetAllTeacherProfiles_ShouldReturnAllProfiles()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var user1 = new User { FirstName = "Teacher1", LastName = "One", UserType = "teacher" };
                var user2 = new User { FirstName = "Teacher2", LastName = "Two", UserType = "teacher" };
                context.Users.AddRange(user1, user2);
                await context.SaveChangesAsync();

                context.TeacherProfiles.AddRange(
                    new TeacherProfile { Id = user1.Id },
                    new TeacherProfile { Id = user2.Id }
                );
                await context.SaveChangesAsync();

                // Act
                var profiles = await context.TeacherProfiles.ToListAsync();

                // Assert
                Assert.Equal(2, profiles.Count);
                Assert.Contains(profiles, p => p.Id == user1.Id);
                Assert.Contains(profiles, p => p.Id == user2.Id);
            }
        }

        [Fact]
        public async Task DeleteTeacherProfile_ShouldWork()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var user = new User
                {
                    FirstName = "Delete",
                    LastName = "Teacher",
                    UserType = "teacher"
                };
                context.Users.Add(user);
                await context.SaveChangesAsync();

                var teacherProfile = new TeacherProfile
                {
                    Id = user.Id
                };
                context.TeacherProfiles.Add(teacherProfile);
                await context.SaveChangesAsync();

                // Act
                context.TeacherProfiles.Remove(teacherProfile);
                await context.SaveChangesAsync();

                // Assert
                var profileInDb = await context.TeacherProfiles.FindAsync(user.Id);
                Assert.Null(profileInDb);
            }
        }
    }
}
