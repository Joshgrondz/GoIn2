using System;
using System.Collections.Generic;

namespace WebApplication1.Models;

public partial class Event
{
    public int Id { get; set; }

    public string? EventName { get; set; }

    public DateOnly EventDate { get; set; }

    public string? EventLocation { get; set; }

    public bool Status { get; set; }

    public int Teacherid { get; set; }

    public int Geofenceid { get; set; }

    public virtual ICollection<ClassEvent> ClassEvents { get; set; } = new List<ClassEvent>();

    public virtual GeoFence? Geofence { get; set; }

    public virtual ICollection<Log> Logs { get; set; } = new List<Log>();

    public virtual ICollection<Notification> Notifications { get; set; } = new List<Notification>();

    public virtual ICollection<Pair> Pairs { get; set; } = new List<Pair>();

    public virtual TeacherProfile Teacher { get; set; } = null!;
}
