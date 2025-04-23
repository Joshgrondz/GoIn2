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
    public class MostRecentStudentLocationViewController : ControllerBase
    {
        private readonly GoIn2Context _context;

        public MostRecentStudentLocationViewController(GoIn2Context context)
        {
            _context = context;
        }

        [HttpGet]
        [SwaggerOperation(Summary = "Get All Locations", Description = "Retrieves a list of all student locations from the database.")]
        public async Task<ActionResult<IEnumerable<MostRecentStudentLocationView>>> GetMostRecentStudentLocationsViews()
        {
            return await _context.MostRecentStudentLocationViews.ToListAsync();
        }


        /*
        //get student location by Student ID
        [HttpGet("{studentId}")]
        public async Task<ActionResult<MostRecentStudentLocationView>> GetMostRecentStudentLocationByStudentId(int studentId)
        {
            var MostRecentByStudentId = await _context.MostRecentStudentLocationViews.FindAsync(studentId);

            if (MostRecentByStudentId == null)
            {
                return NotFound();
            }

            return MostRecentByStudentId;
        }
        */


        // Get all student locations by event ID
        [HttpGet("by-event/{eventid}")]
        public async Task<ActionResult<List<MostRecentStudentLocationView>>> GetMostRecentStudentLocationsByEventId(int eventid)
        {
            var locations = await _context.MostRecentStudentLocationViews
                .Where(loc => loc.Eventid == eventid)
                .ToListAsync();

            if (locations == null || locations.Count == 0)
            {
                return NotFound();
            }


            return locations;
        }



    }
}
