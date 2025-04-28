using System;
using System.Collections.Generic;

namespace WebApplication1.Models;

public partial class ActiveEventsView
{
    public int EventId { get; set; }

    public string? EventName { get; set; }

    public DateOnly? EventDate { get; set; }

    public string? EventLocation { get; set; }

    public int Teacherid { get; set; }

    public int? Geofenceid { get; set; }
}
