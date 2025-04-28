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
        public async Task<ActionResult<IEnumerable<StudentProfileReadDto>>> GetStudentProfiles()
        {
            return await _context.StudentProfiles
                .Select(sp => new StudentProfileReadDto
                {
                    Id = sp.Id,
                    GradeLevel = sp.GradeLevel
                })
                .ToListAsync();
        }

        // GET: api/StudentProfile/5
        [HttpGet("{id}")]
        public async Task<ActionResult<StudentProfileReadDto>> GetStudentProfile(int id)
        {
            var sp = await _context.StudentProfiles.FindAsync(id);

            if (sp == null)
            {
                return NotFound();
            }

            return new StudentProfileReadDto
            {
                Id = sp.Id,
                GradeLevel = sp.GradeLevel
            };
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
        [HttpPost]
        public async Task<ActionResult<StudentProfileReadDto>> PostStudentProfile(StudentProfileCreateDto dto)
        {
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
            await _context.SaveChangesAsync();

            var result = new StudentProfileReadDto
            {
                Id = studentProfile.Id,
                GradeLevel = studentProfile.GradeLevel
            };

            return CreatedAtAction(nameof(GetStudentProfile), new { id = result.Id }, result);
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
