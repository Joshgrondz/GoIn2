﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using WebApplication1.Models;

namespace WebApplication1.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class ClassRosterController : ControllerBase
    {
        private readonly GoIn2Context _context;

        public ClassRosterController(GoIn2Context context)
        {
            _context = context;
        }

        // GET: api/ClassRoster
        [HttpGet]
        public async Task<ActionResult<IEnumerable<ClassRoster>>> GetClassRosters()
        {
            return await _context.ClassRosters.ToListAsync();
        }

        // GET: api/ClassRoster/5
        [HttpGet("{id}")]
        public async Task<ActionResult<ClassRoster>> GetClassRoster(int id)
        {
            var classRoster = await _context.ClassRosters.FindAsync(id);

            if (classRoster == null)
            {
                return NotFound();
            }

            return classRoster;
        }

        // PUT: api/ClassRoster/5
        // To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        [HttpPut("{id}")]
        public async Task<IActionResult> PutClassRoster(int id, ClassRoster classRoster)
        {
            if (id != classRoster.Id)
            {
                return BadRequest();
            }

            _context.Entry(classRoster).State = EntityState.Modified;

            try
            {
                await _context.SaveChangesAsync();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!ClassRosterExists(id))
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

        // POST: api/ClassRoster
        // To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        [HttpPost]
        public async Task<ActionResult<ClassRoster>> PostClassRoster(ClassRoster classRoster)
        {
            _context.ClassRosters.Add(classRoster);
            await _context.SaveChangesAsync();

            return CreatedAtAction("GetClassRoster", new { id = classRoster.Id }, classRoster);
        }

        // DELETE: api/ClassRoster/5
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteClassRoster(int id)
        {
            var classRoster = await _context.ClassRosters.FindAsync(id);
            if (classRoster == null)
            {
                return NotFound();
            }

            _context.ClassRosters.Remove(classRoster);
            await _context.SaveChangesAsync();

            return NoContent();
        }

        private bool ClassRosterExists(int id)
        {
            return _context.ClassRosters.Any(e => e.Id == id);
        }
    }
}
