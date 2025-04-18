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
    public class PairController : ControllerBase
    {
        private readonly GoIn2Context _context;

        public PairController(GoIn2Context context)
        {
            _context = context;
        }

        // GET: api/Pair
        [HttpGet]
        public async Task<ActionResult<IEnumerable<PairReadDto>>> GetPairs()
        {
            return await _context.Pairs
                .Select(p => new PairReadDto
                {
                    Id = p.Id,
                    Student1id = p.Student1id,
                    Student2id = p.Student2id,
                    Eventid = p.Eventid,
                    Status = p.Status
                })
                .ToListAsync();
        }

        // GET: api/Pair/5
        [HttpGet("{id}")]
        public async Task<ActionResult<PairReadDto>> GetPair(int id)
        {
            var p = await _context.Pairs.FindAsync(id);

            if (p == null)
            {
                return NotFound();
            }

            return new PairReadDto
            {
                Id = p.Id,
                Student1id = p.Student1id,
                Student2id = p.Student2id,
                Eventid = p.Eventid,
                Status = p.Status
            };
        }

        // PUT: api/Pair/5
        // To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        [HttpPut("{id}")]
        public async Task<IActionResult> PutPair(int id, Pair pair)
        {
            if (id != pair.Id)
            {
                return BadRequest();
            }

            _context.Entry(pair).State = EntityState.Modified;

            try
            {
                await _context.SaveChangesAsync();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!PairExists(id))
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

        // POST: api/Pair
        [HttpPost]
        public async Task<ActionResult<PairReadDto>> PostPair(PairCreateDto dto)
        {
            var p = new Pair
            {
                Student1id = dto.Student1id,
                Student2id = dto.Student2id,
                Eventid = dto.Eventid,
                Status = dto.Status
            };

            _context.Pairs.Add(p);
            await _context.SaveChangesAsync();

            var result = new PairReadDto
            {
                Id = p.Id,
                Student1id = p.Student1id,
                Student2id = p.Student2id,
                Eventid = p.Eventid,
                Status = p.Status
            };

            return CreatedAtAction(nameof(GetPair), new { id = result.Id }, result);
        }

        // DELETE: api/Pair/5
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeletePair(int id)
        {
            var pair = await _context.Pairs.FindAsync(id);
            if (pair == null)
            {
                return NotFound();
            }

            _context.Pairs.Remove(pair);
            await _context.SaveChangesAsync();

            return NoContent();
        }

        private bool PairExists(int id)
        {
            return _context.Pairs.Any(e => e.Id == id);
        }
    }
}
