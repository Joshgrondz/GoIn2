using Microsoft.EntityFrameworkCore;
using WebApplication1.Models;
using WebApplication1.Dto;

namespace TestProject1
{
    public class UserTest
    {
        private readonly DbContextOptions<GoIn2Context> _options;

        public UserTest()
        {
            _options = new DbContextOptionsBuilder<GoIn2Context>()
                .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString()) // fresh db per test
                .Options;
        }

        [Fact]
        public async Task CreateUserDirectly_ShouldWork()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var user = new User
                {
                    FirstName = "Test",
                    LastName = "User",
                    UserType = "student"
                };

                context.Users.Add(user);
                await context.SaveChangesAsync();

                // Assert
                var createdUser = await context.Users.FirstOrDefaultAsync(u => u.FirstName == "Test" && u.LastName == "User");
                Assert.NotNull(createdUser);
                Assert.Equal("student", createdUser.UserType);
            }
        }

        [Fact]
        public async Task DeleteUserDirectly_ShouldWork()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var user = new User
                {
                    FirstName = "Delete",
                    LastName = "Me",
                    UserType = "teacher"
                };
                context.Users.Add(user);
                await context.SaveChangesAsync();

                // Act
                context.Users.Remove(user);
                await context.SaveChangesAsync();

                // Assert
                var userInDb = await context.Users.FindAsync(user.Id);
                Assert.Null(userInDb);
            }
        }

        [Fact]
        public async Task UpdateUserDirectly_ShouldWork()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var user = new User
                {
                    FirstName = "Old",
                    LastName = "Name",
                    UserType = "teacher"
                };
                context.Users.Add(user);
                await context.SaveChangesAsync();

                // Act
                user.FirstName = "New";
                user.LastName = "Name";
                context.Users.Update(user);
                await context.SaveChangesAsync();

                // Assert
                var updatedUser = await context.Users.FindAsync(user.Id);
                Assert.Equal("New", updatedUser.FirstName);
                Assert.Equal("Name", updatedUser.LastName);
            }
        }

        [Fact]
        public async Task GetUserById_ShouldReturnCorrectUser()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var user = new User
                {
                    FirstName = "Single",
                    LastName = "User",
                    UserType = "student"
                };
                context.Users.Add(user);
                await context.SaveChangesAsync();

                // Act
                var foundUser = await context.Users.FindAsync(user.Id);

                // Assert
                Assert.NotNull(foundUser);
                Assert.Equal("Single", foundUser.FirstName);
                Assert.Equal("User", foundUser.LastName);
            }
        }

        [Fact]
        public async Task GetAllUsers_ShouldReturnAllUsers()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var users = new List<User>
                {
                    new User { FirstName = "User1", LastName = "One", UserType = "student" },
                    new User { FirstName = "User2", LastName = "Two", UserType = "teacher" }
                };

                context.Users.AddRange(users);
                await context.SaveChangesAsync();

                // Act
                var userList = await context.Users.ToListAsync();

                // Assert
                Assert.Equal(2, userList.Count);
                Assert.Contains(userList, u => u.FirstName == "User1");
                Assert.Contains(userList, u => u.FirstName == "User2");
            }
        }

        [Fact]
        public async Task CreateStudentUser_ShouldAlsoAllowCreatingStudentProfileManually()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var user = new User
                {
                    FirstName = "Student",
                    LastName = "Profile",
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
                var profileInDb = await context.StudentProfiles.FirstOrDefaultAsync(sp => sp.Id == user.Id);

                // Assert
                Assert.NotNull(profileInDb);
                Assert.Equal("11th", profileInDb.GradeLevel);
            }
        }
    }
}
