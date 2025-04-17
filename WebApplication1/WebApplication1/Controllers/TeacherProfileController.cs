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
    public class TeacherProfileController : ControllerBase
    {
        private readonly GoIn2Context _context;

        public TeacherProfileController(GoIn2Context context)
        {
            _context = context;
        }

        // GET: api/TeacherProfile
        [HttpGet]
        public async Task<ActionResult<IEnumerable<TeacherProfile>>> GetTeacherProfiles()
        {
            return await _context.TeacherProfiles.ToListAsync();
        }

        // GET: api/TeacherProfile/5
        [HttpGet("{id}")]
        public async Task<ActionResult<TeacherProfile>> GetTeacherProfile(int id)
        {
            var teacherProfile = await _context.TeacherProfiles.FindAsync(id);

            if (teacherProfile == null)
            {
                return NotFound();
            }

            return teacherProfile;
        }

        // PUT: api/TeacherProfile/5
        // To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        [HttpPut("{id}")]
        public async Task<IActionResult> PutTeacherProfile(int id, TeacherProfile teacherProfile)
        {
            if (id != teacherProfile.Id)
            {
                return BadRequest();
            }

            _context.Entry(teacherProfile).State = EntityState.Modified;

            try
            {
                await _context.SaveChangesAsync();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!TeacherProfileExists(id))
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

        // POST: api/TeacherProfile
        [HttpPost]
        public async Task<ActionResult<TeacherProfileCreateDto>> PostTeacherProfile(TeacherProfileCreateDto dto)
        {
            // Make sure the User exists first (optional check)
            var userExists = await _context.Users.AnyAsync(u => u.Id == dto.Id);
            if (!userExists)
            {
                return BadRequest($"No User found with ID {dto.Id}");
            }

            var teacherProfile = new TeacherProfile
            {
                Id = dto.Id
            };

            _context.TeacherProfiles.Add(teacherProfile);

            try
            {
                await _context.SaveChangesAsync();
            }
            catch (DbUpdateException)
            {
                if (TeacherProfileExists(teacherProfile.Id))
                {
                    return Conflict();
                }
                else
                {
                    throw;
                }
            }

            var result = new TeacherProfileReadDto
            {
                Id = teacherProfile.Id
            };

            return CreatedAtAction(nameof(GetTeacherProfile), new { id = result.Id }, result);
        }

        // DELETE: api/TeacherProfile/5
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteTeacherProfile(int id)
        {
            var teacherProfile = await _context.TeacherProfiles.FindAsync(id);
            if (teacherProfile == null)
            {
                return NotFound();
            }

            _context.TeacherProfiles.Remove(teacherProfile);
            await _context.SaveChangesAsync();

            return NoContent();
        }

        private bool TeacherProfileExists(int id)
        {
            return _context.TeacherProfiles.Any(e => e.Id == id);
        }
    }
}
