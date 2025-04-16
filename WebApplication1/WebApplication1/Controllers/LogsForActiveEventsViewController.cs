using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using WebApplication1.Models;

namespace WebApplication1.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class LogsForActiveEventsViewController : ControllerBase
    {
        private readonly GoIn2Context _context;

        public LogsForActiveEventsViewController(GoIn2Context context)
        {
            _context = context;
        }
    }
}
