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
    public class ClassController : ControllerBase
    {
        private readonly GoIn2Context _context;

        public ClassController(GoIn2Context context)
        {
            _context = context;
        }

        // GET: api/Class
        [HttpGet]
        public async Task<ActionResult<IEnumerable<ClassReadDto>>> GetClasses()
        {
            return await _context.Classes
                .Select(c => new ClassReadDto
                {
                    Id = c.Id,
                    Teacherid = c.Teacherid,
                    ClassName = c.ClassName
                })
                .ToListAsync();
        }

        // GET: api/Class/5
        [HttpGet("{id}")]
        public async Task<ActionResult<ClassReadDto>> GetClass(int id)
        {
            var c = await _context.Classes.FindAsync(id);

            if (c == null)
            {
                return NotFound();
            }

            return new ClassReadDto
            {
                Id = c.Id,
                Teacherid = c.Teacherid,
                ClassName = c.ClassName
            };
        }

        // PUT: api/Class/5
        // To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        [HttpPut("{id}")]
        public async Task<IActionResult> PutClass(int id, Class @class)
        {
            if (id != @class.Id)
            {
                return BadRequest();
            }

            _context.Entry(@class).State = EntityState.Modified;

            try
            {
                await _context.SaveChangesAsync();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!ClassExists(id))
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

        // POST: api/Class
        [HttpPost]
        public async Task<ActionResult<ClassReadDto>> PostClass(ClassCreateDto dto)
        {
            var classEntity = new Class
            {
                Teacherid = dto.Teacherid,
                ClassName = dto.ClassName
            };

            _context.Classes.Add(classEntity);
            await _context.SaveChangesAsync();

            var result = new ClassReadDto
            {
                Id = classEntity.Id,
                Teacherid = classEntity.Teacherid,
                ClassName = classEntity.ClassName
            };

            return CreatedAtAction(nameof(GetClass), new { id = result.Id }, result);
        }

        // DELETE: api/Class/5
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteClass(int id)
        {
            var classEntity = await _context.Classes.FindAsync(id);
            if (classEntity == null)
            {
                return NotFound();
            }

            // Get all student IDs linked to the class
            var rosterEntries = await _context.ClassRosters
                .Where(cr => cr.Classid == id)
                .ToListAsync();

            var studentIds = rosterEntries.Select(r => r.Studentid).Distinct().ToList();

            // Remove the roster entries first
            _context.ClassRosters.RemoveRange(rosterEntries);

            // Then delete all users + student profiles tied to those student IDs
            foreach (var studentId in studentIds)
            {
                var user = await _context.Users.FindAsync(studentId);
                if (user != null && user.UserType.ToLower() == "student")
                {
                    // This mimics what your User DELETE already does
                    _context.Pairs.RemoveRange(_context.Pairs.Where(p => p.Student1id == studentId || p.Student2id == studentId));
                    _context.Locations.RemoveRange(_context.Locations.Where(l => l.Userid == studentId));
                    _context.Notifications.RemoveRange(_context.Notifications.Where(n => n.Userid == studentId));
                    

                    var studentProfile = await _context.StudentProfiles.FindAsync(studentId);
                    if (studentProfile != null)
                    {
                        _context.StudentProfiles.Remove(studentProfile);
                    }

                    _context.Users.Remove(user);
                }
            }

            // Now finally remove the class itself
            _context.Classes.Remove(classEntity);

            await _context.SaveChangesAsync();

            return NoContent();
        }


        private bool ClassExists(int id)
        {
            return _context.Classes.Any(e => e.Id == id);
        }
    }
}
