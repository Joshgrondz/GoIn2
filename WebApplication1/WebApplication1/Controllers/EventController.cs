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
    public class EventController : ControllerBase
    {
        private readonly GoIn2Context _context;

        public EventController(GoIn2Context context)
        {
            _context = context;
        }

        // GET: api/Event
        [HttpGet]
        public async Task<ActionResult<IEnumerable<EventReadDto>>> GetEvents()
        {
            return await _context.Events
                .Select(e => new EventReadDto
                {
                    Id = e.Id,
                    EventName = e.EventName,
                    EventDate = e.EventDate,
                    EventLocation = e.EventLocation,
                    Status = e.Status,
                    Teacherid = e.Teacherid,
                    Geofenceid = e.Geofenceid
                })
                .ToListAsync();
        }

        // GET: api/Event/5
        [HttpGet("{id}")]
        public async Task<ActionResult<EventReadDto>> GetEvent(int id)
        {
            var e = await _context.Events.FindAsync(id);

            if (e == null)
            {
                return NotFound();
            }

            return new EventReadDto
            {
                Id = e.Id,
                EventName = e.EventName,
                EventDate = e.EventDate,
                EventLocation = e.EventLocation,
                Status = e.Status,
                Teacherid = e.Teacherid,
                Geofenceid = e.Geofenceid
            };
        }

        // PUT: api/Event/5
        [HttpPut("{id}")]
        public async Task<IActionResult> PutEvent(int id, EventUpdateDto dto)
        {
            var @event = await _context.Events.FindAsync(id);
            if (@event == null)
            {
                return NotFound();
            }

            @event.EventName = dto.EventName;
            @event.EventDate = dto.EventDate;
            @event.EventLocation = dto.EventLocation;
            @event.Status = dto.Status;
            @event.Teacherid = dto.Teacherid;
            @event.Geofenceid = dto.Geofenceid;

            await _context.SaveChangesAsync();

            return NoContent();
        }

        // POST: api/Event
        [HttpPost]
        public async Task<ActionResult<EventReadDto>> PostEvent(EventCreateDto dto)
        {
            var eventEntity = new Event
            {
                EventName = dto.EventName,
                EventDate = dto.EventDate,
                EventLocation = dto.EventLocation,
                Status = dto.Status,
                Teacherid = dto.Teacherid,
                Geofenceid = dto.Geofenceid
            };

            _context.Events.Add(eventEntity);
            await _context.SaveChangesAsync();

            var result = new EventReadDto
            {
                Id = eventEntity.Id,
                EventName = eventEntity.EventName,
                EventDate = eventEntity.EventDate,
                EventLocation = eventEntity.EventLocation,
                Status = eventEntity.Status,
                Teacherid = eventEntity.Teacherid,
                Geofenceid = eventEntity.Geofenceid
            };

            return CreatedAtAction(nameof(GetEvent), new { id = result.Id }, result);
        }

        // DELETE: api/Event/5
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteEvent(int id)
        {
            var @event = await _context.Events.FindAsync(id);
            if (@event == null)
            {
                return NotFound();
            }

            _context.Events.Remove(@event);
            await _context.SaveChangesAsync();

            return NoContent();
        }

        private bool EventExists(int id)
        {
            return _context.Events.Any(e => e.Id == id);
        }
    }
}
