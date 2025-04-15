using System;
using System.Collections.Generic;

namespace WebApplication1.Models;

public partial class ClassEvent
{
    public int Id { get; set; }

    public int Classid { get; set; }

    public int Eventid { get; set; }

    public virtual Class Class { get; set; } = null!;

    public virtual Event Event { get; set; } = null!;
}
