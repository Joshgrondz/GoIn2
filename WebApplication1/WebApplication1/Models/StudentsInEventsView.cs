using System;
using System.Collections.Generic;

namespace WebApplication1.Models;

public partial class StudentsInEventsView
{
    public int EventId { get; set; }

    public string? EventName { get; set; }

    public int StudentId { get; set; }

    public string FirstName { get; set; } = null!;

    public string LastName { get; set; } = null!;
}
