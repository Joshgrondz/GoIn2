using System;
using System.Collections.Generic;

namespace WebApplication1.Models;

public partial class ClassRoster
{
    public int Id { get; set; }

    public int Classid { get; set; }

    public int Studentid { get; set; }

    public virtual Class Class { get; set; } = null!;

    public virtual StudentProfile Student { get; set; } = null!;
}
