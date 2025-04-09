using System;
using System.Collections.Generic;

namespace WebApplication1.Models;

public partial class SampleLocation
{
    public int Id { get; set; }

    public int UserId { get; set; }

    public double Latitude { get; set; }

    public double Longitude { get; set; }

    public double? Accuracy { get; set; }

    public long? TimestampMs { get; set; }

    public string? Provider { get; set; }
}
