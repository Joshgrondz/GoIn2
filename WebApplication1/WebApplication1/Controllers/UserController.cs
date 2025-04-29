using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using WebApplication1.Models;
using WebApplication1.Dto;

namespace WebApplication1.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class UserController : ControllerBase
    {
        private readonly GoIn2Context _context;

        public UserController(GoIn2Context context)
        {
            _context = context;
        }

        // GET: api/User
        [HttpGet]
        public async Task<ActionResult<IEnumerable<UserReadDto>>> GetUsers()
        {
            return await _context.Users
                .Select(u => new UserReadDto
                {
                    Id = u.Id,
                    FirstName = u.FirstName,
                    LastName = u.LastName,
                    UserType = u.UserType
                })
                .ToListAsync();
        }

        // GET: api/User/5
        [HttpGet("{id}")]
        public async Task<ActionResult<UserReadDto>> GetUser(int id)
        {
            var user = await _context.Users.FindAsync(id);

            if (user == null)
            {
                return NotFound();
            }

            return new UserReadDto
            {
                Id = user.Id,
                FirstName = user.FirstName,
                LastName = user.LastName,
                UserType = user.UserType
            };
        }

        // PUT: api/User/5
        // To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        [HttpPut("{id}")]
        public async Task<IActionResult> PutUser(int id, User user)
        {
            if (id != user.Id)
            {
                return BadRequest();
            }

            _context.Entry(user).State = EntityState.Modified;

            try
            {
                await _context.SaveChangesAsync();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!UserExists(id))
                {
                    return NotFound();
                }
                else
                {
                    throw;
                }
            }

            return NoContent();
        }

        // POST: api/User
        [HttpPost]
        public async Task<ActionResult<UserReadDto>> PostUser(UserCreateDto dto)
        {
            var user = new User
            {
                FirstName = dto.FirstName,
                LastName = dto.LastName,
                UserType = dto.UserType
            };

            _context.Users.Add(user);
            await _context.SaveChangesAsync();

            // Automatically create related profile
            if (dto.UserType.ToLower() == "student")
            {
                var studentProfile = new StudentProfile
                {
                    Id = user.Id,
                    GradeLevel = null // default or update if needed
                };
                _context.StudentProfiles.Add(studentProfile);
            }
            else if (dto.UserType.ToLower() == "teacher")
            {
                var teacherProfile = new TeacherProfile
                {
                    Id = user.Id
                };
                _context.TeacherProfiles.Add(teacherProfile);
            }

            await _context.SaveChangesAsync();

            var result = new UserReadDto
            {
                Id = user.Id,
                FirstName = user.FirstName,
                LastName = user.LastName,
                UserType = user.UserType
            };

            return CreatedAtAction(nameof(GetUser), new { id = result.Id }, result);
        }


        // DELETE: api/User/5
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteUser(int id)
        {
            var user = await _context.Users.FindAsync(id);
            if (user == null)
            {
                return NotFound();
            }

            var userType = user.UserType.Trim().ToLower();

            if (userType == "student")
            {
                // Delete student's related records
                _context.ClassRosters.RemoveRange(_context.ClassRosters.Where(cr => cr.Studentid == id));
                _context.Pairs.RemoveRange(_context.Pairs.Where(p => p.Student1id == id || p.Student2id == id));
                _context.Locations.RemoveRange(_context.Locations.Where(l => l.Userid == id));
                _context.Notifications.RemoveRange(_context.Notifications.Where(n => n.Userid == id));

                var studentProfile = await _context.StudentProfiles.FindAsync(id);
                if (studentProfile != null)
                {
                    _context.StudentProfiles.Remove(studentProfile);
                }
            }
            else if (userType == "teacher")
            {
                // Delete teacher's events and related data
                var events = _context.Events.Where(e => e.Teacherid == id).ToList();
                foreach (var ev in events)
                {
                    _context.Logs.RemoveRange(_context.Logs.Where(l => l.Eventid == ev.Id));
                    _context.Pairs.RemoveRange(_context.Pairs.Where(p => p.Eventid == ev.Id));
                    _context.Notifications.RemoveRange(_context.Notifications.Where(n => n.Eventid == ev.Id));
                    _context.ClassEvents.RemoveRange(_context.ClassEvents.Where(ce => ce.Eventid == ev.Id));
                }
                _context.Events.RemoveRange(events);

                // Delete teacher's classes and related data
                var classes = _context.Classes.Where(c => c.Teacherid == id).ToList();
                foreach (var cls in classes)
                {
                    _context.ClassRosters.RemoveRange(_context.ClassRosters.Where(cr => cr.Classid == cls.Id));
                    _context.ClassEvents.RemoveRange(_context.ClassEvents.Where(ce => ce.Classid == cls.Id));
                }
                _context.Classes.RemoveRange(classes);

                // Delete GeoFences assigned to teacher-owned events (optional logic if you track ownership)
                // (If not directly tied to teacherid, this may be unnecessary)

                var teacherProfile = await _context.TeacherProfiles.FindAsync(id);
                if (teacherProfile != null)
                {
                    _context.TeacherProfiles.Remove(teacherProfile);
                }
            }

            // Finally, delete the User
            _context.Users.Remove(user);
            await _context.SaveChangesAsync();

            return NoContent();
        }


        private bool UserExists(int id)
        {
            return _context.Users.Any(e => e.Id == id);
        }
    }
}
