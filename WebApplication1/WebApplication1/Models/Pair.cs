using System;
using System.Collections.Generic;

namespace WebApplication1.Models;

public partial class Pair
{
    public int Id { get; set; }

    public int Student1id { get; set; }

    public int Student2id { get; set; }

    public int Eventid { get; set; }

    public bool Status { get; set; }

    public virtual Event? Event { get; set; }

    public virtual ICollection<Message> Messages { get; set; } = new List<Message>();

    public virtual StudentProfile? Student1 { get; set; }

    public virtual StudentProfile? Student2 { get; set; }
}
