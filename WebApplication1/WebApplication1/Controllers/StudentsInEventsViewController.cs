using WebApplication1.Models;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace WebApplication1.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class StudentsInEventsViewController : ControllerBase
    {
        private readonly GoIn2Context _context;

        public StudentsInEventsViewController(GoIn2Context context)
        {
            _context = context;
        }

        [HttpGet]
        public async Task<ActionResult<IEnumerable<StudentsInEventsView>>> GetStudentProfiles()
        {
            return await _context.StudentsInEventsViews.ToListAsync();
        }

        // Get all students by event ID
        [HttpGet("{eventid}")]
        public async Task<ActionResult<List<StudentsInEventsView>>> GetStudentsInEventViewByEventId(int eventid)
        {
            var students = await _context.StudentsInEventsViews
                .Where(loc => loc.EventId == eventid)
                .ToListAsync();

            if (students == null || students.Count == 0)
            {
                return NotFound();
            }

            return students;
        }
    }
}
