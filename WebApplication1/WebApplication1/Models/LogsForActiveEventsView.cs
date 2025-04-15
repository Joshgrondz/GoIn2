using System;
using System.Collections.Generic;

namespace WebApplication1.Models;

public partial class LogsForActiveEventsView
{
    public int LogId { get; set; }

    public int? Eventid { get; set; }

    public string? EventName { get; set; }

    public string? LogDescription { get; set; }

    public DateTime? Timestamp { get; set; }
}
