using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using WebApplication1.Dto;
using WebApplication1.Models;

namespace WebApplication1.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class StudentProfileController : ControllerBase
    {
        private readonly GoIn2Context _context;

        public StudentProfileController(GoIn2Context context)
        {
            _context = context;
        }

        // GET: api/StudentProfile
        [HttpGet]
        public async Task<ActionResult<IEnumerable<StudentProfile>>> GetStudentProfiles()
        {
            return await _context.StudentProfiles.ToListAsync();
        }

        // GET: api/StudentProfile/5
        [HttpGet("{id}")]
        public async Task<ActionResult<StudentProfile>> GetStudentProfile(int id)
        {
            var studentProfile = await _context.StudentProfiles.FindAsync(id);

            if (studentProfile == null)
            {
                return NotFound();
            }

            return studentProfile;
        }

        // PUT: api/StudentProfile/5
        // To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        [HttpPut("{id}")]
        public async Task<IActionResult> PutStudentProfile(int id, StudentProfile studentProfile)
        {
            if (id != studentProfile.Id)
            {
                return BadRequest();
            }

            _context.Entry(studentProfile).State = EntityState.Modified;

            try
            {
                await _context.SaveChangesAsync();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!StudentProfileExists(id))
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

        // POST: api/StudentProfile
        // To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        [HttpPost]
        public async Task<ActionResult<StudentProfile>> PostStudentProfile(StudentProfileCreateDto dto)
        {
            // Optional: check if User exists
            var userExists = await _context.Users.AnyAsync(u => u.Id == dto.Id);
            if (!userExists)
            {
                return BadRequest($"User with ID {dto.Id} does not exist.");
            }

            var studentProfile = new StudentProfile
            {
                Id = dto.Id,
                GradeLevel = dto.GradeLevel
            };

            _context.StudentProfiles.Add(studentProfile);

            try
            {
                await _context.SaveChangesAsync();
            }
            catch (DbUpdateException)
            {
                if (StudentProfileExists(studentProfile.Id))
                {
                    return Conflict();
                }
                else
                {
                    throw;
                }
            }

            return CreatedAtAction(nameof(GetStudentProfile), new { id = studentProfile.Id }, studentProfile);
        }

        // DELETE: api/StudentProfile/5
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteStudentProfile(int id)
        {
            var studentProfile = await _context.StudentProfiles.FindAsync(id);
            if (studentProfile == null)
            {
                return NotFound();
            }

            _context.StudentProfiles.Remove(studentProfile);
            await _context.SaveChangesAsync();

            return NoContent();
        }

        private bool StudentProfileExists(int id)
        {
            return _context.StudentProfiles.Any(e => e.Id == id);
        }
    }
}
