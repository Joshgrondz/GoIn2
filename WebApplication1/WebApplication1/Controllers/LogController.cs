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
    public class LogController : ControllerBase
    {
        private readonly GoIn2Context _context;

        public LogController(GoIn2Context context)
        {
            _context = context;
        }

        // GET: api/Log
        [HttpGet]
        public async Task<ActionResult<IEnumerable<LogReadDto>>> GetLogs()
        {
            return await _context.Logs
                .Select(log => new LogReadDto
                {
                    Id = log.Id,
                    Eventid = log.Eventid,
                    LogDescription = log.LogDescription,
                    Timestamp = log.Timestamp
                })
                .ToListAsync();
        }

        // GET: api/Log/5
        [HttpGet("{id}")]
        public async Task<ActionResult<LogReadDto>> GetLog(int id)
        {
            var log = await _context.Logs.FindAsync(id);

            if (log == null)
            {
                return NotFound();
            }

            return new LogReadDto
            {
                Id = log.Id,
                Eventid = log.Eventid,
                LogDescription = log.LogDescription,
                Timestamp = log.Timestamp
            };
        }

        // PUT: api/Log/5
        // To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        [HttpPut("{id}")]
        public async Task<IActionResult> PutLog(int id, Log log)
        {
            if (id != log.Id)
            {
                return BadRequest();
            }

            _context.Entry(log).State = EntityState.Modified;

            try
            {
                await _context.SaveChangesAsync();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!LogExists(id))
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

        // POST: api/Log
        [HttpPost]
        public async Task<ActionResult<LogReadDto>> PostLog(LogCreateDto dto)
        {
            var log = new Log
            {
                Eventid = dto.Eventid,
                LogDescription = dto.LogDescription,
                Timestamp = dto.Timestamp
            };

            _context.Logs.Add(log);
            await _context.SaveChangesAsync();

            var result = new LogReadDto
            {
                Id = log.Id,
                Eventid = log.Eventid,
                LogDescription = log.LogDescription,
                Timestamp = log.Timestamp
            };

            return CreatedAtAction(nameof(GetLog), new { id = result.Id }, result);
        }

        // DELETE: api/Log/5
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteLog(int id)
        {
            var log = await _context.Logs.FindAsync(id);
            if (log == null)
            {
                return NotFound();
            }

            _context.Logs.Remove(log);
            await _context.SaveChangesAsync();

            return NoContent();
        }

        [HttpGet("Event/{eventId}")]
        public async Task<ActionResult<IEnumerable<LogReadDto>>> GetLogsByEventId(int eventId)
        {
            
            var logs = await _context.Logs
                .Where(log => log.Eventid == eventId) 
                .Select(log => new LogReadDto
                {
                    Id = log.Id,
                    Eventid = log.Eventid,
                    LogDescription = log.LogDescription,
                    Timestamp = log.Timestamp
                })
                .ToListAsync();

            
            return logs;
        }

        private bool LogExists(int id)
        {
            return _context.Logs.Any(e => e.Id == id);
        }
    }
}
