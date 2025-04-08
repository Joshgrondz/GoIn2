using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Swashbuckle.AspNetCore.Annotations;
using WebApplication1.Database;
using WebApplication1.Models;

namespace WebApplication1.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class StudentController : ControllerBase
    {
        private readonly TableContext _context;

        public StudentController(TableContext context)
        {
            _context = context;
        }

        [HttpGet]
        [SwaggerOperation(Summary = "Get All Students", Description = "Retrieves a list of all Students from the database.")]
        public async Task<ActionResult<IEnumerable<Student>>> GetStudent()
        {
            try
            {
                var Students = await _context.Student
                    .Select(g => new Student
                    {
                        ID = g.ID,
                        Name = g.Name,
                    })
                    .ToListAsync();

                return Ok(Students);
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error in GetGames: {ex.Message}");
                return StatusCode(500, "Internal Server Error");
            }
        }
    }
}
