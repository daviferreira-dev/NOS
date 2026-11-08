package com.nos.systems;

import com.nos.data.Tuning;

/** Tentativas da sessão; só encontros ainda não coletados podem recarregar. */
public final class InkPot {
    private int remaining = Tuning.MAX_ATTEMPTS;
    public boolean spend() { remaining = Math.max(0, remaining - 1); return remaining > 0; }
    public void refill() { remaining = Math.min(Tuning.MAX_ATTEMPTS, remaining + 1); }
    public int getRemaining() { return remaining; }
}
