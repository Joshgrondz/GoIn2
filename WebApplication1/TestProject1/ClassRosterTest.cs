using Microsoft.EntityFrameworkCore;
using WebApplication1.Models;
using Xunit;

namespace TestProject1
{
    public class ClassRosterTest
    {
        private readonly DbContextOptions<GoIn2Context> _options;

        public ClassRosterTest()
        {
            _options = new DbContextOptionsBuilder<GoIn2Context>()
                .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString()) // fresh database per test class
                .Options;
        }

        [Fact]
        public async Task AddStudentToClass_ShouldWork()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var teacher = new User { FirstName = "Teacher", LastName = "Roster", UserType = "teacher" };
                var student = new User { FirstName = "Student", LastName = "One", UserType = "student" };

                context.Users.AddRange(teacher, student);
                await context.SaveChangesAsync();

                var classEntity = new Class
                {
                    Teacherid = teacher.Id,
                    ClassName = "Biology"
                };
                context.Classes.Add(classEntity);
                await context.SaveChangesAsync();

                var rosterEntry = new ClassRoster
                {
                    Classid = classEntity.Id,
                    Studentid = student.Id
                };

                // Act
                context.ClassRosters.Add(rosterEntry);
                await context.SaveChangesAsync();

                // Assert
                var createdRoster = await context.ClassRosters.FindAsync(rosterEntry.Id);
                Assert.NotNull(createdRoster);
                Assert.Equal(classEntity.Id, createdRoster.Classid);
                Assert.Equal(student.Id, createdRoster.Studentid);
            }
        }

        [Fact]
        public async Task GetClassRosterById_ShouldReturnCorrectEntry()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var teacher = new User { FirstName = "Roster", LastName = "Teacher", UserType = "teacher" };
                var student = new User { FirstName = "Roster", LastName = "Student", UserType = "student" };

                context.Users.AddRange(teacher, student);
                await context.SaveChangesAsync();

                var classEntity = new Class
                {
                    Teacherid = teacher.Id,
                    ClassName = "Chemistry"
                };
                context.Classes.Add(classEntity);
                await context.SaveChangesAsync();

                var rosterEntry = new ClassRoster
                {
                    Classid = classEntity.Id,
                    Studentid = student.Id
                };
                context.ClassRosters.Add(rosterEntry);
                await context.SaveChangesAsync();

                // Act
                var foundRoster = await context.ClassRosters.FindAsync(rosterEntry.Id);

                // Assert
                Assert.NotNull(foundRoster);
                Assert.Equal(classEntity.Id, foundRoster.Classid);
                Assert.Equal(student.Id, foundRoster.Studentid);
            }
        }

        [Fact]
        public async Task GetAllClassRosters_ShouldReturnAllEntries()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var teacher = new User { FirstName = "Multi", LastName = "Teacher", UserType = "teacher" };
                var student1 = new User { FirstName = "Student1", LastName = "One", UserType = "student" };
                var student2 = new User { FirstName = "Student2", LastName = "Two", UserType = "student" };

                context.Users.AddRange(teacher, student1, student2);
                await context.SaveChangesAsync();

                var classEntity = new Class
                {
                    Teacherid = teacher.Id,
                    ClassName = "Physics"
                };
                context.Classes.Add(classEntity);
                await context.SaveChangesAsync();

                var rosterEntries = new List<ClassRoster>
                {
                    new ClassRoster { Classid = classEntity.Id, Studentid = student1.Id },
                    new ClassRoster { Classid = classEntity.Id, Studentid = student2.Id }
                };
                context.ClassRosters.AddRange(rosterEntries);
                await context.SaveChangesAsync();

                // Act
                var allRosters = await context.ClassRosters.ToListAsync();

                // Assert
                Assert.Equal(2, allRosters.Count);
                Assert.Contains(allRosters, r => r.Studentid == student1.Id);
                Assert.Contains(allRosters, r => r.Studentid == student2.Id);
            }
        }

        [Fact]
        public async Task DeleteClassRosterEntry_ShouldWork()
        {
            using (var context = new GoIn2Context(_options))
            {
                // Arrange
                var teacher = new User { FirstName = "Teacher", LastName = "Delete", UserType = "teacher" };
                var student = new User { FirstName = "Student", LastName = "Delete", UserType = "student" };

                context.Users.AddRange(teacher, student);
                await context.SaveChangesAsync();

                var classEntity = new Class
                {
                    Teacherid = teacher.Id,
                    ClassName = "Art"
                };
                context.Classes.Add(classEntity);
                await context.SaveChangesAsync();

                var rosterEntry = new ClassRoster
                {
                    Classid = classEntity.Id,
                    Studentid = student.Id
                };
                context.ClassRosters.Add(rosterEntry);
                await context.SaveChangesAsync();

                // Act
                context.ClassRosters.Remove(rosterEntry);
                await context.SaveChangesAsync();

                // Assert
                var deletedRoster = await context.ClassRosters.FindAsync(rosterEntry.Id);
                Assert.Null(deletedRoster);
            }
        }
    }
}
