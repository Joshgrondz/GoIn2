using Microsoft.EntityFrameworkCore;
using WebApplication1.Models;
using Xunit;

namespace TestProject1
{
    public class ClassTest
    {
        private readonly DbContextOptions<GoIn2Context> _options;

        public ClassTest()
        {
            _options = new DbContextOptionsBuilder<GoIn2Context>()
                .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString()) // fresh db per test
                .Options;
        }

        [Fact]
        public async Task CreateClass_ShouldWork()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var teacher = new User
                {
                    FirstName = "Class",
                    LastName = "Teacher",
                    UserType = "teacher"
                };
                context.Users.Add(teacher);
                await context.SaveChangesAsync();

                var classEntity = new Class
                {
                    Teacherid = teacher.Id,
                    ClassName = "History 101"
                };

                // Act
                context.Classes.Add(classEntity);
                await context.SaveChangesAsync();

                // Assert
                var createdClass = await context.Classes.FindAsync(classEntity.Id);
                Assert.NotNull(createdClass);
                Assert.Equal("History 101", createdClass.ClassName);
                Assert.Equal(teacher.Id, createdClass.Teacherid);
            }
        }

        [Fact]
        public async Task GetClassById_ShouldReturnCorrectClass()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var teacher = new User
                {
                    FirstName = "Teacher",
                    LastName = "Example",
                    UserType = "teacher"
                };
                context.Users.Add(teacher);
                await context.SaveChangesAsync();

                var classEntity = new Class
                {
                    Teacherid = teacher.Id,
                    ClassName = "Math 201"
                };
                context.Classes.Add(classEntity);
                await context.SaveChangesAsync();

                // Act
                var foundClass = await context.Classes.FindAsync(classEntity.Id);

                // Assert
                Assert.NotNull(foundClass);
                Assert.Equal("Math 201", foundClass.ClassName);
                Assert.Equal(teacher.Id, foundClass.Teacherid);
            }
        }

        [Fact]
        public async Task GetAllClasses_ShouldReturnAllClasses()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var teacher = new User
                {
                    FirstName = "Teacher",
                    LastName = "Multi",
                    UserType = "teacher"
                };
                context.Users.Add(teacher);
                await context.SaveChangesAsync();

                var classes = new List<Class>
                {
                    new Class { Teacherid = teacher.Id, ClassName = "Physics" },
                    new Class { Teacherid = teacher.Id, ClassName = "Chemistry" }
                };
                context.Classes.AddRange(classes);
                await context.SaveChangesAsync();

                // Act
                var classList = await context.Classes.ToListAsync();

                // Assert
                Assert.Equal(2, classList.Count);
                Assert.Contains(classList, c => c.ClassName == "Physics");
                Assert.Contains(classList, c => c.ClassName == "Chemistry");
            }
        }

        [Fact]
        public async Task UpdateClass_ShouldWork()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var teacher = new User
                {
                    FirstName = "Teacher",
                    LastName = "Update",
                    UserType = "teacher"
                };
                context.Users.Add(teacher);
                await context.SaveChangesAsync();

                var classEntity = new Class
                {
                    Teacherid = teacher.Id,
                    ClassName = "Science"
                };
                context.Classes.Add(classEntity);
                await context.SaveChangesAsync();

                // Act
                classEntity.ClassName = "Advanced Science";
                context.Classes.Update(classEntity);
                await context.SaveChangesAsync();

                // Assert
                var updatedClass = await context.Classes.FindAsync(classEntity.Id);
                Assert.Equal("Advanced Science", updatedClass.ClassName);
            }
        }

        [Fact]
        public async Task DeleteClass_ShouldWork()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var teacher = new User
                {
                    FirstName = "Teacher",
                    LastName = "Delete",
                    UserType = "teacher"
                };
                context.Users.Add(teacher);
                await context.SaveChangesAsync();

                var classEntity = new Class
                {
                    Teacherid = teacher.Id,
                    ClassName = "Art"
                };
                context.Classes.Add(classEntity);
                await context.SaveChangesAsync();

                // Act
                context.Classes.Remove(classEntity);
                await context.SaveChangesAsync();

                // Assert
                var deletedClass = await context.Classes.FindAsync(classEntity.Id);
                Assert.Null(deletedClass);
            }
        }
    }
}
