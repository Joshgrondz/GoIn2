using System;
using System.Collections.Generic;

namespace WebApplication1.Models;

public partial class MostRecentStudentLocationView
{
    public int StudentId { get; set; }
    public string FirstName { get; set; } = null!;
    public string LastName { get; set; } = null!;
    public int? Eventid { get; set; }
    public string? EventName { get; set; }
    public double Latitude { get; set; }
    public double Longitude { get; set; }
    public long? TimestampMs { get; set; }
}
