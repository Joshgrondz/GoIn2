using System;
using System.Collections.Generic;

namespace WebApplication1.Models;

public partial class Class
{
    public int Id { get; set; }

    public int Teacherid { get; set; }

    public string? ClassName { get; set; }

    public virtual ICollection<ClassEvent> ClassEvents { get; set; } = new List<ClassEvent>();

    public virtual ICollection<ClassRoster> ClassRosters { get; set; } = new List<ClassRoster>();

    public virtual TeacherProfile? Teacher { get; set; }
}
