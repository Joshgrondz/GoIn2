using System;
using System.Collections.Generic;

namespace WebApplication1.Models;

public partial class Log
{
    public int Id { get; set; }

    public int Eventid { get; set; }

    public string? LogDescription { get; set; }

    public DateTime Timestamp { get; set; }

    public virtual Event? Event { get; set; }
}
