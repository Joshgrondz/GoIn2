using System;
using System.Collections.Generic;

namespace WebApplication1.Models;

public partial class GeoFence
{
    public int Id { get; set; }

    public int EventRadius { get; set; }

    public int TeacherRadius { get; set; }

    public double PairDistance { get; set; }

    public double Latitude { get; set; }

    public double Longitude { get; set; }

    public virtual ICollection<Event> Events { get; set; } = new List<Event>();
}
