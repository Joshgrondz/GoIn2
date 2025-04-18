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
    public class GeoFenceController : ControllerBase
    {
        private readonly GoIn2Context _context;

        public GeoFenceController(GoIn2Context context)
        {
            _context = context;
        }

        // GET: api/GeoFence
        [HttpGet]
        public async Task<ActionResult<IEnumerable<GeoFenceReadDto>>> GetGeoFences()
        {
            return await _context.GeoFences
                .Select(g => new GeoFenceReadDto
                {
                    Id = g.Id,
                    EventRadius = g.EventRadius,
                    TeacherRadius = g.TeacherRadius,
                    PairDistance = g.PairDistance,
                    Latitude = g.Latitude,
                    Longitude = g.Longitude
                })
                .ToListAsync();
        }

        // GET: api/GeoFence/5
        [HttpGet("{id}")]
        public async Task<ActionResult<GeoFenceReadDto>> GetGeoFence(int id)
        {
            var geo = await _context.GeoFences.FindAsync(id);

            if (geo == null)
            {
                return NotFound();
            }

            return new GeoFenceReadDto
            {
                Id = geo.Id,
                EventRadius = geo.EventRadius,
                TeacherRadius = geo.TeacherRadius,
                PairDistance = geo.PairDistance,
                Latitude = geo.Latitude,
                Longitude = geo.Longitude
            };
        }

        // PUT: api/GeoFence/5
        // To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        [HttpPut("{id}")]
        public async Task<IActionResult> PutGeoFence(int id, GeoFence geoFence)
        {
            if (id != geoFence.Id)
            {
                return BadRequest();
            }

            _context.Entry(geoFence).State = EntityState.Modified;

            try
            {
                await _context.SaveChangesAsync();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!GeoFenceExists(id))
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

        // POST: api/GeoFence
        [HttpPost]
        public async Task<ActionResult<GeoFenceReadDto>> PostGeoFence(GeoFenceCreateDto dto)
        {
            var geo = new GeoFence
            {
                EventRadius = dto.EventRadius,
                TeacherRadius = dto.TeacherRadius,
                PairDistance = dto.PairDistance,
                Latitude = dto.Latitude,
                Longitude = dto.Longitude
            };

            _context.GeoFences.Add(geo);
            await _context.SaveChangesAsync();

            var result = new GeoFenceReadDto
            {
                Id = geo.Id,
                EventRadius = geo.EventRadius,
                TeacherRadius = geo.TeacherRadius,
                PairDistance = geo.PairDistance,
                Latitude = geo.Latitude,
                Longitude = geo.Longitude
            };

            return CreatedAtAction(nameof(GetGeoFence), new { id = result.Id }, result);
        }

        // DELETE: api/GeoFence/5
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteGeoFence(int id)
        {
            var geoFence = await _context.GeoFences.FindAsync(id);
            if (geoFence == null)
            {
                return NotFound();
            }

            _context.GeoFences.Remove(geoFence);
            await _context.SaveChangesAsync();

            return NoContent();
        }

        private bool GeoFenceExists(int id)
        {
            return _context.GeoFences.Any(e => e.Id == id);
        }
    }
}
