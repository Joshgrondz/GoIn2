using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Swashbuckle.AspNetCore.Annotations;
using WebApplication1.Models;

namespace WebApplication1.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class SampleLocationsController : ControllerBase
    {
        private readonly GoIn2Context _context;

        public SampleLocationsController(GoIn2Context context)
        {
            _context = context;
        }

        // GET: api/SampleLocations
        [HttpGet]
        [SwaggerOperation(Summary = "Get All Locations", Description = "Retrieves a list of all locations from the database.")]
        public async Task<ActionResult<IEnumerable<SampleLocation>>> GetSampleLocations()
        {
            return await _context.SampleLocations.ToListAsync();
        }

        // GET: api/SampleLocations/5
        [HttpGet("{id}")]
        [SwaggerOperation(Summary = "Get Location based on Location ID", Description = "Retrieves a single location based on location ID.")]
        public async Task<ActionResult<SampleLocation>> GetSampleLocation(int id)
        {
            var sampleLocation = await _context.SampleLocations.FindAsync(id);

            if (sampleLocation == null)
            {
                return NotFound();
            }

            return sampleLocation;
        }

        // PUT: api/SampleLocations/5
        // To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        [HttpPut("{id}")]
        [SwaggerOperation(Summary = "Update Location", Description = "Updates location entry based on entry ID.")]
        public async Task<IActionResult> PutSampleLocation(int id, SampleLocation sampleLocation)
        {
            if (id != sampleLocation.Id)
            {
                return BadRequest();
            }

            _context.Entry(sampleLocation).State = EntityState.Modified;

            try
            {
                await _context.SaveChangesAsync();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!SampleLocationExists(id))
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

        // POST: api/SampleLocations
        // To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        [HttpPost]
        [SwaggerOperation(Summary = "Post a Location", Description = "Adds a location to the database.")]
        public async Task<ActionResult<SampleLocation>> PostSampleLocation(SampleLocation sampleLocation)
        {
            _context.SampleLocations.Add(sampleLocation);
            await _context.SaveChangesAsync();

            return CreatedAtAction("GetSampleLocation", new { id = sampleLocation.Id }, sampleLocation);
        }

        // DELETE: api/SampleLocations/5
        [HttpDelete("{id}")]
        [SwaggerOperation(Summary = "Delete a location based on LocationID", Description = "What the summary says dumbass.")]
        public async Task<IActionResult> DeleteSampleLocation(int id)
        {
            var sampleLocation = await _context.SampleLocations.FindAsync(id);
            if (sampleLocation == null)
            {
                return NotFound();
            }

            _context.SampleLocations.Remove(sampleLocation);
            await _context.SaveChangesAsync();

            return NoContent();
        }

        private bool SampleLocationExists(int id)
        {
            return _context.SampleLocations.Any(e => e.Id == id);
        }

        [HttpGet("Student/{studentId}/Latest")]
        [SwaggerOperation(Summary = "Get latest location of student", Description = "Gets the latest location based on a studentID.")]
        public async Task<ActionResult<SampleLocation>> GetLatestLocationByStudentId(int studentId)
        {
            var latestLocation = await _context.SampleLocations
                .Where(sl => sl.UserId == studentId)
                .OrderByDescending(sl => sl.TimestampMs)
                .FirstOrDefaultAsync();

            if (latestLocation == null)
            {
                return NotFound();
            }

            return latestLocation;
        }
    }
}
