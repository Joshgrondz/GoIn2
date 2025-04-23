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
    public class NotificationController : ControllerBase
    {
        private readonly GoIn2Context _context;

        public NotificationController(GoIn2Context context)
        {
            _context = context;
        }

        // GET: api/Notification
        [HttpGet]
        public async Task<ActionResult<IEnumerable<NotificationReadDto>>> GetNotifications()
        {
            return await _context.Notifications
                .Select(n => new NotificationReadDto
                {
                    Id = n.Id,
                    Userid = n.Userid,
                    Eventid = n.Eventid,
                    NotificationDescription = n.NotificationDescription,
                    NotificationTimestamp = n.NotificationTimestamp,
                    Sent = n.Sent
                })
                .ToListAsync();
        }

        // GET: api/Notification/5
        [HttpGet("{id}")]
        public async Task<ActionResult<NotificationReadDto>> GetNotification(int id)
        {
            var n = await _context.Notifications.FindAsync(id);

            if (n == null)
            {
                return NotFound();
            }

            return new NotificationReadDto
            {
                Id = n.Id,
                Userid = n.Userid,
                Eventid = n.Eventid,
                NotificationDescription = n.NotificationDescription,
                NotificationTimestamp = n.NotificationTimestamp,
                Sent = n.Sent
            };
        }

        // PUT: api/Notification/5
        // To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        [HttpPut("{id}")]
        public async Task<IActionResult> PutNotification(int id, Notification notification)
        {
            if (id != notification.Id)
            {
                return BadRequest();
            }

            _context.Entry(notification).State = EntityState.Modified;

            try
            {
                await _context.SaveChangesAsync();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!NotificationExists(id))
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

        // GET: api/Notification/user/5
        [HttpGet("user/{userid}")]
        public async Task<ActionResult<IEnumerable<NotificationReadDto>>> GetNotificationsByUserId(int userid)
        {
            var notifications = await _context.Notifications
                .Where(n => n.Userid == userid)
                .Select(n => new NotificationReadDto
                {
                    Id = n.Id,
                    Userid = n.Userid,
                    Eventid = n.Eventid,
                    NotificationDescription = n.NotificationDescription,
                    NotificationTimestamp = n.NotificationTimestamp,
                    Sent = n.Sent
                })
                .ToListAsync();

            if (notifications == null || !notifications.Any())
            {
                return NotFound();
            }

            return notifications;
        }


        // POST: api/Notification
        [HttpPost]
        public async Task<ActionResult<NotificationReadDto>> PostNotification(NotificationCreateDto dto)
        {
            var n = new Notification
            {
                Userid = dto.Userid,
                Eventid = dto.Eventid,
                NotificationDescription = dto.NotificationDescription,
                NotificationTimestamp = dto.NotificationTimestamp,
                Sent = dto.Sent
            };

            _context.Notifications.Add(n);
            await _context.SaveChangesAsync();

            var result = new NotificationReadDto
            {
                Id = n.Id,
                Userid = n.Userid,
                Eventid = n.Eventid,
                NotificationDescription = n.NotificationDescription,
                NotificationTimestamp = n.NotificationTimestamp,
                Sent = n.Sent
            };

            return CreatedAtAction(nameof(GetNotification), new { id = result.Id }, result);
        }
        // DELETE: api/Notification/5
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteNotification(int id)
        {
            var notification = await _context.Notifications.FindAsync(id);
            if (notification == null)
            {
                return NotFound();
            }

            _context.Notifications.Remove(notification);
            await _context.SaveChangesAsync();

            return NoContent();
        }

        private bool NotificationExists(int id)
        {
            return _context.Notifications.Any(e => e.Id == id);
        }
    }
}
