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
    public class LocationController : ControllerBase
    {
        private readonly GoIn2Context _context;

        public LocationController(GoIn2Context context)
        {
            _context = context;
        }

        // GET: api/Location
        [HttpGet]
        public async Task<ActionResult<IEnumerable<LocationReadDto>>> GetLocations()
        {
            return await _context.Locations
                .Select(l => new LocationReadDto
                {
                    Id = l.Id,
                    Userid = l.Userid,
                    Latitude = l.Latitude,
                    Longitude = l.Longitude,
                    LocAccuracy = l.LocAccuracy,
                    LocAltitude = l.LocAltitude,
                    LocSpeed = l.LocSpeed,
                    LocBearing = l.LocBearing,
                    LocProvider = l.LocProvider,
                    TimestampMs = l.TimestampMs
                })
                .ToListAsync();
        }

        // GET: api/Location/5
        [HttpGet("{id}")]
        public async Task<ActionResult<LocationReadDto>> GetLocation(int id)
        {
            var l = await _context.Locations.FindAsync(id);

            if (l == null)
            {
                return NotFound();
            }

            return new LocationReadDto
            {
                Id = l.Id,
                Userid = l.Userid,
                Latitude = l.Latitude,
                Longitude = l.Longitude,
                LocAccuracy = l.LocAccuracy,
                LocAltitude = l.LocAltitude,
                LocSpeed = l.LocSpeed,
                LocBearing = l.LocBearing,
                LocProvider = l.LocProvider,
                TimestampMs = l.TimestampMs
            };
        }

        // PUT: api/Location/5
        // To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        [HttpPut("{id}")]
        public async Task<IActionResult> PutLocation(int id, Location location)
        {
            if (id != location.Id)
            {
                return BadRequest();
            }

            _context.Entry(location).State = EntityState.Modified;

            try
            {
                await _context.SaveChangesAsync();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!LocationExists(id))
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

        // POST: api/Location
        [HttpPost]
        public async Task<ActionResult<LocationReadDto>> PostLocation(LocationCreateDto dto)
        {
            var location = new Location
            {
                Userid = dto.Userid,
                Latitude = dto.Latitude,
                Longitude = dto.Longitude,
                LocAccuracy = dto.LocAccuracy,
                LocAltitude = dto.LocAltitude,
                LocSpeed = dto.LocSpeed,
                LocBearing = dto.LocBearing,
                LocProvider = dto.LocProvider,
                TimestampMs = dto.TimestampMs
            };

            _context.Locations.Add(location);
            await _context.SaveChangesAsync();

            var result = new LocationReadDto
            {
                Id = location.Id,
                Userid = location.Userid,
                Latitude = location.Latitude,
                Longitude = location.Longitude,
                LocAccuracy = location.LocAccuracy,
                LocAltitude = location.LocAltitude,
                LocSpeed = location.LocSpeed,
                LocBearing = location.LocBearing,
                LocProvider = location.LocProvider,
                TimestampMs = location.TimestampMs
            };

            return CreatedAtAction(nameof(GetLocation), new { id = result.Id }, result);
        }

        // DELETE: api/Location/5
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteLocation(int id)
        {
            var location = await _context.Locations.FindAsync(id);
            if (location == null)
            {
                return NotFound();
            }

            _context.Locations.Remove(location);
            await _context.SaveChangesAsync();

            return NoContent();
        }

        private bool LocationExists(int id)
        {
            return _context.Locations.Any(e => e.Id == id);
        }
    }
}
