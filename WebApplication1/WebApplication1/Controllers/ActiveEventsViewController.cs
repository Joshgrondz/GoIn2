using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using WebApplication1.Models;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace WebApplication1.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class ActiveEventsViewController : ControllerBase
    {
        private readonly GoIn2Context _context;

        public ActiveEventsViewController(GoIn2Context context)
        {
            _context = context;
        }

        // GET: api/ActiveEventsView/eventnames
        [HttpGet("eventnames")]
        public async Task<ActionResult<IEnumerable<string>>> GetActiveEventNames()
        {
            var activeEventNames = await _context.Events
                                                 .Where(e => e.Status == true)
                                                 .Select(e => e.EventName)
                                                 .ToListAsync();

            return Ok(activeEventNames);
        }
    }
}
