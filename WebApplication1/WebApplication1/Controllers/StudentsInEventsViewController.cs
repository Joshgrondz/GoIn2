using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using WebApplication1.Models;

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

        // IMPLEMENT GET METHODS HERE

    }
}
