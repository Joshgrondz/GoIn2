using WebApplication1.Models;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;

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
    }
}
