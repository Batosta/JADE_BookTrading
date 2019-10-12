# JADE_BookTrading

In this scenario there are some agents selling books and other agents buying books on behalf of their users. 
Each buyer agent receives the title of the book to buy (the “target book”) and periodically requests all known seller agents to provide an offer. As soon as an offer is received, the buyer agent accepts it and issues a purchase order. If more than one seller agent provides an offer the buyer agent accepts the best one (lowest price). Having bought the target book the buyer agent terminates.

Each seller agent has a minimal GUI by means of which the user can insert new titles (and the
associated price) in the local catalogue of books for sale. Seller agents continuously wait for requests from
buyer agents. When asked to provide an offer for a book they check if the requested book is in their
catalogue and in this case reply with the price. Otherwise they refuse. When they receive a purchase order
they serve it and remove the requested book from their catalogue.

All issues related to electronic payment are outside the scope of this and are not taken into account. 
