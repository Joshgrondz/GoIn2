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
    public class ClassEventController : ControllerBase
    {
        private readonly GoIn2Context _context;

        public ClassEventController(GoIn2Context context)
        {
            _context = context;
        }

        // GET: api/ClassEvent
        [HttpGet]
        public async Task<ActionResult<IEnumerable<ClassEventReadDto>>> GetClassEvents()
        {
            return await _context.ClassEvents
                .Select(ce => new ClassEventReadDto
                {
                    Id = ce.Id,
                    Classid = ce.Classid,
                    Eventid = ce.Eventid
                })
                .ToListAsync();
        }

        // GET: api/ClassEvent/5
        [HttpGet("{id}")]
        public async Task<ActionResult<ClassEventReadDto>> GetClassEvent(int id)
        {
            var ce = await _context.ClassEvents.FindAsync(id);

            if (ce == null)
            {
                return NotFound();
            }

            return new ClassEventReadDto
            {
                Id = ce.Id,
                Classid = ce.Classid,
                Eventid = ce.Eventid
            };
        }

        // PUT: api/ClassEvent/5
        // To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        [HttpPut("{id}")]
        public async Task<IActionResult> PutClassEvent(int id, ClassEvent classEvent)
        {
            if (id != classEvent.Id)
            {
                return BadRequest();
            }

            _context.Entry(classEvent).State = EntityState.Modified;

            try
            {
                await _context.SaveChangesAsync();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!ClassEventExists(id))
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

        // POST: api/ClassEvent
        [HttpPost]
        public async Task<ActionResult<ClassEventReadDto>> PostClassEvent(ClassEventCreateDto dto)
        {
            var classEvent = new ClassEvent
            {
                Classid = dto.Classid,
                Eventid = dto.Eventid
            };

            _context.ClassEvents.Add(classEvent);
            await _context.SaveChangesAsync();

            var result = new ClassEventReadDto
            {
                Id = classEvent.Id,
                Classid = classEvent.Classid,
                Eventid = classEvent.Eventid
            };

            return CreatedAtAction(nameof(GetClassEvent), new { id = result.Id }, result);
        }

        // DELETE: api/ClassEvent/5
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteClassEvent(int id)
        {
            var classEvent = await _context.ClassEvents.FindAsync(id);
            if (classEvent == null)
            {
                return NotFound();
            }

            _context.ClassEvents.Remove(classEvent);
            await _context.SaveChangesAsync();

            return NoContent();
        }

        private bool ClassEventExists(int id)
        {
            return _context.ClassEvents.Any(e => e.Id == id);
        }
    }
}
