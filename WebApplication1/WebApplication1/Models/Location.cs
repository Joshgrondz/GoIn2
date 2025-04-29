using System;
using System.Collections.Generic;

namespace WebApplication1.Models;

public partial class Location
{
    public int Id { get; set; }

    public int Userid { get; set; }

    public double Latitude { get; set; }

    public double Longitude { get; set; }

    public double LocAccuracy { get; set; }

    public double LocAltitude { get; set; }

    public double LocSpeed { get; set; }

    public double LocBearing { get; set; }

    public string? LocProvider { get; set; }

    public long TimestampMs { get; set; }

    public virtual User User { get; set; } = null!;
}
