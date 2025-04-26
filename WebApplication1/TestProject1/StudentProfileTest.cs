using Microsoft.EntityFrameworkCore;
using WebApplication1.Models;
using Xunit;

namespace TestProject1
{
    public class StudentProfileTest
    {
        private readonly DbContextOptions<GoIn2Context> _options;

        public StudentProfileTest()
        {
            _options = new DbContextOptionsBuilder<GoIn2Context>()
                .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString()) // fresh database per test class
                .Options;
        }

        [Fact]
        public async Task CreateStudentProfile_ShouldWork()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var user = new User
                {
                    FirstName = "Student",
                    LastName = "Example",
                    UserType = "student"
                };
                context.Users.Add(user);
                await context.SaveChangesAsync();

                var studentProfile = new StudentProfile
                {
                    Id = user.Id,
                    GradeLevel = "10th"
                };

                // Act
                context.StudentProfiles.Add(studentProfile);
                await context.SaveChangesAsync();

                // Assert
                var createdProfile = await context.StudentProfiles.FindAsync(user.Id);
                Assert.NotNull(createdProfile);
                Assert.Equal(user.Id, createdProfile.Id);
                Assert.Equal("10th", createdProfile.GradeLevel);
            }
        }

        [Fact]
        public async Task GetStudentProfileById_ShouldReturnCorrectProfile()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var user = new User
                {
                    FirstName = "Jane",
                    LastName = "Student",
                    UserType = "student"
                };
                context.Users.Add(user);
                await context.SaveChangesAsync();

                var studentProfile = new StudentProfile
                {
                    Id = user.Id,
                    GradeLevel = "11th"
                };
                context.StudentProfiles.Add(studentProfile);
                await context.SaveChangesAsync();

                // Act
                var foundProfile = await context.StudentProfiles.FindAsync(user.Id);

                // Assert
                Assert.NotNull(foundProfile);
                Assert.Equal(user.Id, foundProfile.Id);
                Assert.Equal("11th", foundProfile.GradeLevel);
            }
        }

        [Fact]
        public async Task GetAllStudentProfiles_ShouldReturnAllProfiles()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var user1 = new User { FirstName = "Student1", LastName = "One", UserType = "student" };
                var user2 = new User { FirstName = "Student2", LastName = "Two", UserType = "student" };
                context.Users.AddRange(user1, user2);
                await context.SaveChangesAsync();

                context.StudentProfiles.AddRange(
                    new StudentProfile { Id = user1.Id, GradeLevel = "9th" },
                    new StudentProfile { Id = user2.Id, GradeLevel = "12th" }
                );
                await context.SaveChangesAsync();

                // Act
                var profiles = await context.StudentProfiles.ToListAsync();

                // Assert
                Assert.Equal(2, profiles.Count);
                Assert.Contains(profiles, p => p.Id == user1.Id && p.GradeLevel == "9th");
                Assert.Contains(profiles, p => p.Id == user2.Id && p.GradeLevel == "12th");
            }
        }

        [Fact]
        public async Task DeleteStudentProfile_ShouldWork()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var user = new User
                {
                    FirstName = "Delete",
                    LastName = "Student",
                    UserType = "student"
                };
                context.Users.Add(user);
                await context.SaveChangesAsync();

                var studentProfile = new StudentProfile
                {
                    Id = user.Id,
                    GradeLevel = "8th"
                };
                context.StudentProfiles.Add(studentProfile);
                await context.SaveChangesAsync();

                // Act
                context.StudentProfiles.Remove(studentProfile);
                await context.SaveChangesAsync();

                // Assert
                var profileInDb = await context.StudentProfiles.FindAsync(user.Id);
                Assert.Null(profileInDb);
            }
        }
    }
}
