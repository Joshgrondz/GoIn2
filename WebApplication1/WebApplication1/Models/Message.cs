using System;
using System.Collections.Generic;

namespace WebApplication1.Models;

public partial class Message
{
    public int Id { get; set; }

    public int? Senderid { get; set; }

    public int? Recipientid { get; set; }

    public int? Pairid { get; set; }

    public string? MessageText { get; set; }

    public DateTime? SentAt { get; set; }

    public virtual Pair? Pair { get; set; }

    public virtual User? Recipient { get; set; }

    public virtual User? Sender { get; set; }
}
